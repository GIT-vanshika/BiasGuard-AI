from pydantic import BaseModel
from typing import List, Dict, Optional, Any

class UploadResponse(BaseModel):
    status: str
    message: str
    filename: str
    columns_detected: List[str]

class AuditConfig(BaseModel):
    protected_attributes: List[str]
    prediction_col: str
    target_col: str

class MetricDetail(BaseModel):
    score: float
    is_biased: bool
    group_rates: Dict[str, float]

class Metrics(BaseModel):
    demographic_parity: MetricDetail
    equal_opportunity: MetricDetail

class BiasAuditResponse(BaseModel):
    status: str
    metrics: Metrics
    intersectional_max_bias: float
    proxy_features_detected: List[str]

class LLMExplanationResponse(BaseModel):
    executive_summary: str
    counterfactual_insight: str
    fallback_used: bool = False

class DriftAlert(BaseModel):
    alert: bool
    message: str

class MonitoringHistoryItem(BaseModel):
    timestamp: str
    dp_score: float

class MonitoringResponse(BaseModel):
    history: List[MonitoringHistoryItem]
    drift_status: DriftAlert
