package com.example.sapworkpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadResponseDto(
    val filename: String,
    @SerializedName("chunks_added") val chunksAdded: Int,
    @SerializedName("total_documents") val totalDocuments: Int,
    @SerializedName("total_chunks") val totalChunks: Int
)

data class DocumentStatsDto(
    @SerializedName("total_documents") val totalDocuments: Int,
    @SerializedName("total_chunks") val totalChunks: Int
)

data class DocumentListItemDto(
    val filename: String,
    val chunks: Int,
    @SerializedName("uploaded_at") val uploadedAt: Double? = null
)