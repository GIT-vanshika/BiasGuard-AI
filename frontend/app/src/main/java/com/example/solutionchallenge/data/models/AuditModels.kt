package com.example.solutionchallenge.data.models

data class MetricDetail(
    val score: Double,
    val is_biased: Boolean,
    val group_rates: Map<String, Double>
)

data class Metrics(
    val demographic_parity: MetricDetail?,
    val equal_opportunity: MetricDetail?
)

data class RunAuditResponse(
    val status: String,
    val metrics: Metrics,
    val intersectional_max_bias: Double,
    val proxy_features_detected: List<String>
)
