# BiasGuard AI Backend Structure

This document provides a comprehensive breakdown of every component, file, and function within the BiasGuard AI backend architecture.

## 📁 Root Directory (`/`)

### `main.py`
The absolute entry point of the FastAPI application. It binds together the routing, dependencies, and external services.
- **`app = FastAPI(...)`**: Initializes the web server instance.
- **Middleware**: Configured with `CORSMiddleware` to allow frontend/mobile clients to communicate with the API without CORS blockages.
- **State Management**: Uses a global `latest_file_path` variable to temporarily store the uploaded CSV in memory between requests.
- **Endpoints:**
  - `GET /` : Health check route to ensure the server is online.
  - `POST /upload-data`: Accepts a `.csv` Multipart form file. Reads the headers to enforce the data contract (requires `prediction` and `actual` columns). Saves the file to a temporary system directory.
  - `POST /run-bias-audit`: Reads form data for `protected_attributes`. Automatically pulls the CSV from memory, runs demographic parity, equal opportunity, intersectional bias, and proxy feature calculations via `services/bias_engine.py`. Logs the result to the monitoring system.
  - `POST /get-explanations`: Takes the output metrics from the audit and passes them to the LLM engine to generate human-readable explanations.
  - `GET /monitor-bias`: Retrieves historical drift data and formatting it for a dashboard view.

### `models.py`
Contains all the Pydantic data schemas. These ensure strict type-checking and validation for incoming requests and outgoing responses.
- **`UploadResponse`**: Schema confirming file upload success and listing the detected columns.
- **`AuditConfig`**: Schema for the variables required to run an audit.
- **`MetricDetail` & `Metrics`**: Objects structuring the statistical thresholds (e.g., scores, `is_biased` flags, and varying group rates).
- **`BiasAuditResponse`**: The heavy payload containing all parsed fairness metrics, proxy features, and max intersectional bias.
- **`LLMExplanationResponse`**: Structures the returned AI output (`executive_summary`, `counterfactual_insight`).
- **`MonitoringHistoryItem`, `DriftAlert`, `MonitoringResponse`**: Schemas representing system drifts continuously over time.

### `requirements.txt`
The dependency lockfile essential for running the environment.
- `fastapi`, `uvicorn`: For the web framework and ASGI server.
- `pandas`, `numpy`: Data manipulation, handling the uploaded datasets.
- `scikit-learn`: Mathematical analysis and calculating proxies.
- `google-genai`: The SDK used to communicate with the LLM.
- `python-multipart`: Required by FastAPI to process physical file uploads.
- `pydantic`: For data serialization.

### `.venv/` & `__pycache__/`
- `.venv/`: The isolated Python virtual environment housing all installed dependencies.
- `__pycache__/`: Automatically generated compiled bytecode to make the Python scripts execute faster.

---

## 📁 `services/` Directory

This folder holds the core business logic, entirely decoupled from the HTTP routing in `main.py`.

### `bias_engine.py`
The mathematical heart of the system.
- `calculate_demographic_parity()`: Checks if the positive prediction rate is equal across all demographic groups.
- `calculate_equal_opportunity()`: Looks at the *actual* positive targets and checks if the model accurately predicts them equally across different groups.
- `calculate_intersectional_bias()`: Combines multiple protected attributes (e.g., Race + Gender) to detect compounding discrimination that single metrics might miss.
- `find_proxy_features()`: Runs Pearson correlation coefficients against the dataset to find seemingly harmless columns (like "zip code") that might be secretly correlating with protected attributes (like "race").

### `llm_explanation.py`
Handles translating complex math into business logic.
- `generate_summary_and_recommendation()`: Ingests raw JSON data (DP scores, Equal Opportunity metrics, proxies) and structures a prompt. It sends this context to an LLM to generate an "Executive Summary" and actionable "Counterfactual Insights" for stakeholders.

### `monitoring.py`
Provides system statefulness and historical tracking over time.
- `log_audit_result()`: Takes the newest Demographic Parity score and appends it to a local CSV (`bias_history.csv`) with an ISO timestamp.
- `get_history()`: Reads the stored `.csv` log and returns it as a list of dictionaries.
- `check_drift()`: Compares the absolute difference between the *current* bias score and the *previously logged* bias score against a hardcoded `DRIFT_THRESHOLD` (currently 0.1). If the drift exceeds this, it flags it as a `CRITICAL` alert.
