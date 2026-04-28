package com.example.solutionchallenge.data.models

data class AuditResult(
    val id: String,
    val datasetName: String,
    val demographicParity: Float,
    val equalOpportunity: Float,
    val status: String // "Fair" or "Unfair"
)

data class Explanation(
    val whyDecisionHappened: String,
    val suggestedFix: String,
    val counterfactualInsight: String
)

data class DriftMonitor(
    val previousAudits: List<AuditResult>,
    val currentDriftScore: Float,
    val alerts: List<Alert>
)

data class Alert(
    val title: String,
    val message: String,
    val severity: String // "WARNING", "CRITICAL"
)
