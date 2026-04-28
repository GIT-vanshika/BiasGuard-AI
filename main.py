import os
import shutil
import tempfile
import pandas as pd
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import List
from models import (
    UploadResponse, BiasAuditResponse, LLMExplanationResponse, MonitoringResponse,
    Metrics, MetricDetail, DriftAlert, MonitoringHistoryItem
)
from services.bias_engine import (
    calculate_demographic_parity, calculate_equal_opportunity,
    calculate_intersectional_bias, find_proxy_features
)
from services.llm_explanation import generate_summary_and_recommendation
from services.monitoring import log_audit_result, check_drift, get_history

app = FastAPI(title="BiasGuard AI", description="MVP Backend API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from fastapi import Request

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    import traceback
    with open("crash.log", "a") as f_log:
        f_log.write(f"\n--- 500 SERVER CRASH on {request.url.path} ---\n")
        traceback.print_exc(file=f_log)
    return JSONResponse(status_code=500, content={"detail": f"CRASH LOG: {str(exc)}"})

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    with open("crash.log", "a") as f_log:
        f_log.write(f"\n--- 422 ANDROID FORMATTING ERROR on {request.url.path} ---\n")
        f_log.write(f"HEADERS: {dict(request.headers)}\n")
        f_log.write(f"DETAILS: {str(exc.errors())}\n")
    return JSONResponse(status_code=422, content={"detail": exc.errors()})


latest_file_path = None

@app.get("/")
async def root():
    return {
        "status": "online",
        "message": "Welcome to BiasGuard AI. Please navigate to /docs for the interactive Swagger UI testing."
    }

@app.post("/upload-data", response_model=UploadResponse)
async def upload_data(file: UploadFile = File(...)):
    global latest_file_path
    
    # Android OkHttp/Retrofit often sends files WITHOUT a filename. This causes .endswith() to crash the server with a 500 AttributeError!
    filename = getattr(file, 'filename', None) or 'upload.csv'
    if not filename.endswith('.csv'):
        raise HTTPException(status_code=400, detail="Only CSV files are supported.")
        
    try:
        temp_dir = tempfile.gettempdir()
        # Android sometimes sends weird absolute uris as filenames. Safely extract just the raw name:
        safe_filename = os.path.basename(getattr(file, 'filename', 'upload.csv')) or 'upload.csv'
        file_path = os.path.join(temp_dir, safe_filename)
        
        # Async chunk buffer (officially supported by FastAPI) avoids blocking the event loop or RAM memory issues!
        with open(file_path, "wb") as f:
            while True:
                chunk = await file.read(1024 * 1024) # 1 MB chunks
                if not chunk:
                    break
                f.write(chunk)
            
        latest_file_path = file_path
        
        # FIX 2 & 3 (Memory OOM / Dirty Data Crash): 
        # Skip weird malformed rows and safely truncate at 5000 rows for the hackathon MVP.
        try:
            df = pd.read_csv(file_path, nrows=5000, on_bad_lines='skip')
        except UnicodeDecodeError:
            # Fallback for weird Android encodings (e.g., UTF-16 from Excel Android, or ISO-8859-1)
            df = pd.read_csv(file_path, nrows=5000, on_bad_lines='skip', encoding='latin1')
        except pd.errors.EmptyDataError:
            raise HTTPException(status_code=400, detail="The uploaded CSV file is entirely empty or corrupted.")
            
        # Re-save our perfect, small 5000-row file so `run_bias_audit` doesn't crash on us either!
        df.to_csv(file_path, index=False)
        # We removed the hardcoded 'prediction' and 'actual' check here.
        # Now any CSV can be uploaded! We will validate column names dynamically during the /run-bias-audit step.
            
        return UploadResponse(
            status="success",
            message="File uploaded successfully. We will map the column headers dynamically in the next step.",
            filename=safe_filename,
            columns_detected=df.columns.tolist()
        )
    except HTTPException:
        # Re-raise standard fastAPI exceptions
        raise
    except Exception as e:
        import traceback
        traceback.print_exc()
        # SILENT TRAP: Write the exact bug to a file so the AI can read it on the next turn!
        try:
            with open("crash.log", "a") as f_log:
                f_log.write(f"\n--- NEW CRASH ---\n")
                traceback.print_exc(file=f_log)
        except Exception:
            pass
            
        raise HTTPException(status_code=500, detail=f"CRASH LOG: {str(e)}")

@app.post("/run-bias-audit", response_model=BiasAuditResponse)
async def run_bias_audit(
    protected_attributes: List[str] = Form(...),
    prediction_col: str = Form("prediction"),
    target_col: str = Form("actual")
):
    global latest_file_path
    if not latest_file_path or not os.path.exists(latest_file_path):
        raise HTTPException(status_code=400, detail="No dataset found in memory. Please call /upload-data first.")
        
    df = pd.read_csv(latest_file_path)
    primary_protected = protected_attributes[0] if protected_attributes else ""
    
    if primary_protected not in df.columns:
        raise HTTPException(status_code=400, detail=f"Protected attribute column '{primary_protected}' not found.")
    if prediction_col not in df.columns:
        raise HTTPException(status_code=400, detail=f"Prediction column '{prediction_col}' not found. Available: {df.columns.tolist()}")
    if target_col not in df.columns:
        raise HTTPException(status_code=400, detail=f"Target column '{target_col}' not found. Available: {df.columns.tolist()}")

    # FORENSIC FIX: Prevent 500 crashes if Android app points target_col to a String column (e.g. "HS-grad")
    # Pandas .mean() crashes on strings. We automatically factorize (label encode) them to numeric (0, 1, 2...)
    import pandas.api.types as ptypes
    for col in [prediction_col, target_col]:
        if not ptypes.is_numeric_dtype(df[col]):
            df[col] = pd.factorize(df[col])[0]

    dp_result = calculate_demographic_parity(df, primary_protected, prediction_col)
    eo_result = calculate_equal_opportunity(df, primary_protected, prediction_col, target_col)
    
    intersectional_max = 0.0
    if len(protected_attributes) > 1:
        intersectional_max = calculate_intersectional_bias(df, protected_attributes, prediction_col)
        
    excluded_cols = protected_attributes + [prediction_col, target_col]
    proxies = find_proxy_features(df, primary_protected, excluded_cols)
    
    log_audit_result(dp_result["score"])
    
    metrics = Metrics(
        demographic_parity=MetricDetail(**dp_result),
        equal_opportunity=MetricDetail(**eo_result)
    )
    
    return BiasAuditResponse(
        status="success",
        metrics=metrics,
        intersectional_max_bias=intersectional_max,
        proxy_features_detected=proxies
    )

@app.post("/get-explanations", response_model=LLMExplanationResponse)
async def get_explanations(payload: BiasAuditResponse):
    metrics_json = payload.dict()
    proxies = payload.proxy_features_detected
    res = generate_summary_and_recommendation(metrics_json, proxies)
    return LLMExplanationResponse(**res)

@app.get("/monitor-bias", response_model=MonitoringResponse)
async def monitor_bias():
    history = get_history()
    if not history:
        return MonitoringResponse(
            history=[],
            drift_status=DriftAlert(alert=False, message="No history found.")
        )
        
    current_bias = float(history[-1]['dp_score'])
    drift_res = check_drift(current_bias)
    
    return MonitoringResponse(
        history=[MonitoringHistoryItem(**h) for h in history],
        drift_status=DriftAlert(**drift_res)
    )

if __name__ == "__main__":
    import uvicorn
    import socket
    
    # Identify local IP address
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Does not actually connect to internet, just routing
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
    except Exception:
        local_ip = "127.0.0.1"
    finally:
        s.close()
        
    print("\n" + "="*60)
    print("🚀 BiasGuard AI Backend is running!")
    print(f"💻 Local Machine Access: http://127.0.0.1:8000")
    print(f"📱 WiFi Network Access: http://{local_ip}:8000")
    print(f"Interactive Swagger UI: http://{local_ip}:8000/docs")
    print("="*60 + "\n")

    port = int(os.environ.get("PORT", 8000))
    print(f"\n🚀 BiasGuard AI starting on port {port}...")
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
