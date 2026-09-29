package com.example.sapworkpilot.data.remote

import com.example.sapworkpilot.data.remote.dto.DocumentStatsDto
import com.example.sapworkpilot.data.remote.dto.UploadResponseDto
import com.example.sapworkpilot.data.remote.dto.DocumentListItemDto
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path

interface DocumentApi {

    @Multipart
    @retrofit2.http.POST("documents/upload")
    suspend fun upload(@Part file: MultipartBody.Part): UploadResponseDto

    @GET("documents/stats")
    suspend fun getStats(): DocumentStatsDto

    @GET("documents/list")
    suspend fun getDocumentList(): List<DocumentListItemDto>

    @DELETE("documents/{filename}")
    suspend fun deleteDocument(@Path("filename") filename: String)
}