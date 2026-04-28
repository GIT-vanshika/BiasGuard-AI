package com.example.solutionchallenge.data.repository

import com.example.solutionchallenge.data.models.Alert
import com.example.solutionchallenge.data.models.AuditResult
import com.example.solutionchallenge.data.models.DriftMonitor
import com.example.solutionchallenge.data.models.Explanation
import com.example.solutionchallenge.data.models.ExplanationResponse
import com.example.solutionchallenge.data.models.MonitorResponse
import com.example.solutionchallenge.data.models.RunAuditResponse
import com.example.solutionchallenge.data.models.UploadResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class MockBiasGuardRepository : BiasGuardRepository {
    override fun runAudit(
        dataset: String,
        targetColumn: String,
        protectedAttribute: String
    ): Flow<Result<AuditResult>> = flow {
        delay(1500) // Simulating network delay
        val randomStatus = listOf("Fair", "Unfair", "Warning").random()
        emit(Result.success(AuditResult(
            id = "audit_${System.currentTimeMillis()}",
            datasetName = dataset,
            demographicParity = 0.85f,
            equalOpportunity = 0.78f,
            status = randomStatus
        )))
    }

    override fun getExplanation(auditId: String): Flow<Result<Explanation>> = flow {
        delay(1000)
        emit(Result.success(Explanation(
            whyDecisionHappened = "The model heavily penalized candidates based on non-relevant factors tied to the protected attribute.",
            suggestedFix = "Consider reweighing the training dataset to balance representation across the protected class.",
            counterfactualInsight = "If this candidate had a different background class, their likelihood of approval would increase by 18%."
        )))
    }

    override fun getDriftMonitor(): Flow<Result<DriftMonitor>> = flow {
        delay(1200)
        emit(Result.success(DriftMonitor(
            previousAudits = listOf(
                AuditResult("1", "loan_db_01", 0.90f, 0.89f, "Fair"),
                AuditResult("2", "loan_db_02", 0.88f, 0.85f, "Fair"),
                AuditResult("3", "loan_db_tmp", 0.76f, 0.70f, "Warning")
            ),
            currentDriftScore = 0.12f,
            alerts = listOf(
                Alert("Bias Threshold Crossed", "Demographic parity dropped below 0.80 on the latest audit stream.", "WARNING"),
                Alert("Model Drift Detected", "Distribution of output scores has shifted significantly since last deployment.", "CRITICAL")
            )
        )))
    }

    // New Integrations
    override fun uploadData(file: File): Flow<Result<UploadResponse>> = flow { }
    override fun runBiasAudit(protectedAttributes: List<String>, predictionCol: String, targetCol: String): Flow<Result<RunAuditResponse>> = flow { }
    override fun getExplanations(): Flow<Result<ExplanationResponse>> = flow { }
    override fun monitorBias(): Flow<Result<MonitorResponse>> = flow { }
}
