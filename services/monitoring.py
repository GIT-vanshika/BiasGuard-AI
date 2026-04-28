import pandas as pd
import os
from datetime import datetime

HISTORY_FILE = "bias_history.csv"
DRIFT_THRESHOLD = 0.1

def log_audit_result(dp_score: float) -> list:
    item = {
        "timestamp": datetime.utcnow().isoformat(),
        "dp_score": dp_score
    }
    df_new = pd.DataFrame([item])
    
    if not os.path.exists(HISTORY_FILE):
        df_new.to_csv(HISTORY_FILE, index=False)
    else:
        df_new.to_csv(HISTORY_FILE, mode='a', header=False, index=False)
    
    return get_history()

def get_history() -> list:
    if not os.path.exists(HISTORY_FILE):
        return []
        
    df = pd.read_csv(HISTORY_FILE)
    return df.to_dict('records')

def check_drift(current_bias: float) -> dict:
    history = get_history()
    # Need at least two points to calculate drift
    if len(history) < 2:
        return {"alert": False, "message": "Stable. Needs more data to calculate historical drift."}
        
    # Get previous bias
    prev_bias = float(history[-2]['dp_score'])
    
    drift = abs(current_bias - prev_bias)
    if drift > DRIFT_THRESHOLD:
        return {"alert": True, "message": f"CRITICAL: Bias drift of {round(drift, 4)} detected! Exceeds threshold of {DRIFT_THRESHOLD}."}
        
    return {"alert": False, "message": f"Drift is stable ({round(drift, 4)})."}
