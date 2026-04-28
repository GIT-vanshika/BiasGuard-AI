package com.example.solutionchallenge.data.repository

import com.example.solutionchallenge.data.models.ExplanationResponse
import com.example.solutionchallenge.data.models.MonitorResponse
import com.example.solutionchallenge.data.models.RunAuditResponse
import com.example.solutionchallenge.data.models.UploadResponse
import com.example.solutionchallenge.data.models.AuditResult
import com.example.solutionchallenge.data.models.DriftMonitor
import com.example.solutionchallenge.data.models.Explanation
import kotlinx.coroutines.flow.Flow
import java.io.File

interface BiasGuardRepository {
    // New Backend Contract Endpoints
    fun uploadData(file: File): Flow<Result<UploadResponse>>
    fun runBiasAudit(protectedAttributes: List<String>, predictionCol: String, targetCol: String): Flow<Result<RunAuditResponse>>
    fun getExplanations(): Flow<Result<ExplanationResponse>>
    fun monitorBias(): Flow<Result<MonitorResponse>>

    // Legacy Support endpoints to avoid breaking unrelated UI Code
    fun runAudit(dataset: String, targetColumn: String, protectedAttribute: String): Flow<Result<AuditResult>>
    fun getExplanation(auditId: String): Flow<Result<Explanation>>
    fun getDriftMonitor(): Flow<Result<DriftMonitor>>
}
