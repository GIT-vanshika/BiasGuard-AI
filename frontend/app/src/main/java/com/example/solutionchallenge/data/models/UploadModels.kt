package com.example.solutionchallenge.data.models

data class UploadResponse(
    val status: String,
    val message: String,
    val filename: String,
    val columns_detected: List<String>
)
