package com.example.solutionchallenge.data.models

data class ExplanationResponse(
    val executive_summary: String,
    val counterfactual_insight: String,
    val fallback_used: Boolean
)
