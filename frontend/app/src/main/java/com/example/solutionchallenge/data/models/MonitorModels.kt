package com.example.solutionchallenge.data.models

data class BiasHistoryPoint(
    val timestamp: String,
    val dp_score: Double
)

data class DriftStatus(
    val alert: Boolean,
    val message: String
)

data class MonitorResponse(
    val history: List<BiasHistoryPoint>,
    val drift_status: DriftStatus
)
