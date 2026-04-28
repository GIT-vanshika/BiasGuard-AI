package com.example.solutionchallenge.data.repository

import com.example.solutionchallenge.data.api.BiasGuardApiService
import com.example.solutionchallenge.data.models.ExplanationResponse
import com.example.solutionchallenge.data.models.MonitorResponse
import com.example.solutionchallenge.data.models.RunAuditResponse
import com.example.solutionchallenge.data.models.UploadResponse
import com.example.solutionchallenge.data.models.AuditResult
import com.example.solutionchallenge.data.models.DriftMonitor
import com.example.solutionchallenge.data.models.Explanation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class RetrofitBiasGuardRepository(
    private val apiService: BiasGuardApiService
) : BiasGuardRepository {

    var latestAudit: RunAuditResponse? = null

    override fun uploadData(file: File): Flow<Result<UploadResponse>> = flow {
        try {
            val mediaType = MediaType.parse("text/csv")
            val requestFile = RequestBody.create(mediaType, file)
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.uploadData(filePart)
            emit(Result.success(response))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrBlank()) errorBody else e.message()
            emit(Result.failure(Exception("Backend Error: $errorMessage", e)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun runBiasAudit(
        protectedAttributes: List<String>,
        predictionCol: String,
        targetCol: String
    ): Flow<Result<RunAuditResponse>> = flow {
        try {
            val response = apiService.runBiasAudit(protectedAttributes, predictionCol, targetCol)
            latestAudit = response
            emit(Result.success(response))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrBlank()) errorBody else e.message()
            emit(Result.failure(Exception("Backend Error: $errorMessage", e)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getExplanations(): Flow<Result<ExplanationResponse>> = flow {
        try {
            val auditResponse = latestAudit ?: throw Exception("Cannot get explanation without running an audit first")
            val response = apiService.getExplanations(auditResponse)
            emit(Result.success(response))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrBlank()) errorBody else e.message()
            emit(Result.failure(Exception("Backend Error: $errorMessage", e)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun monitorBias(): Flow<Result<MonitorResponse>> = flow {
        try {
            val response = apiService.monitorBias()
            emit(Result.success(response))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrBlank()) errorBody else e.message()
            emit(Result.failure(Exception("Backend Error: $errorMessage", e)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Legacy Unused
    override fun runAudit(dataset: String, targetColumn: String, protectedAttribute: String): Flow<Result<AuditResult>> = flow {
        throw NotImplementedError("Legacy method ignored")
    }

    override fun getExplanation(auditId: String): Flow<Result<Explanation>> = flow {
        throw NotImplementedError("Legacy method ignored")
    }

    override fun getDriftMonitor(): Flow<Result<DriftMonitor>> = flow {
        throw NotImplementedError("Legacy method ignored")
    }
}
