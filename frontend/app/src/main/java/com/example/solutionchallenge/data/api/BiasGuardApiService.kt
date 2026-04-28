package com.example.solutionchallenge.data.api

import com.example.solutionchallenge.data.models.ExplanationResponse
import com.example.solutionchallenge.data.models.MonitorResponse
import com.example.solutionchallenge.data.models.RunAuditResponse
import com.example.solutionchallenge.data.models.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface BiasGuardApiService {

    @Multipart
    @POST("upload-data")
    suspend fun uploadData(
        @Part file: MultipartBody.Part
    ): UploadResponse

    @FormUrlEncoded
    @POST("run-bias-audit")
    suspend fun runBiasAudit(
        @Field("protected_attributes") protectedAttributes: List<String>,
        @Field("prediction_col") predictionCol: String,
        @Field("target_col") targetCol: String
    ): RunAuditResponse

    @POST("get-explanations")
    suspend fun getExplanations(
        @Body auditResponse: RunAuditResponse
    ): ExplanationResponse

    @GET("monitor-bias")
    suspend fun monitorBias(): MonitorResponse
}
