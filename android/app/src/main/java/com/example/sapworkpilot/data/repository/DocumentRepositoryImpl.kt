package com.example.sapworkpilot.data.repository

import com.example.sapworkpilot.data.remote.DocumentApi
import com.example.sapworkpilot.domain.repository.DocumentListItem
import com.example.sapworkpilot.domain.repository.DocumentRepository
import com.example.sapworkpilot.domain.repository.DocumentStats
import com.example.sapworkpilot.domain.repository.UploadResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val api: DocumentApi
) : DocumentRepository {

    override suspend fun uploadDocument(fileName: String, fileBytes: ByteArray): Result<UploadResult> {
        return try {
            val mediaType = when {
                fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                fileName.endsWith(".doc", ignoreCase = true) -> "application/msword"
                fileName.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                fileName.endsWith(".csv", ignoreCase = true) -> "text/csv"
                fileName.endsWith(".md", ignoreCase = true) -> "text/markdown"
                fileName.endsWith(".json", ignoreCase = true) -> "application/json"
                else -> "text/plain"
            }.toMediaTypeOrNull()

            val requestBody = fileBytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val response = api.upload(part)
            Result.success(
                UploadResult(
                    filename = response.filename,
                    chunksAdded = response.chunksAdded,
                    stats = DocumentStats(response.totalDocuments, response.totalChunks)
                )
            )
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> "Unsupported or unreadable file"
                403 -> "You don't have permission to upload documents"
                else -> "Server error (${e.code()})"
            }
            Result.failure(Exception(message))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }

    override suspend fun getStats(): Result<DocumentStats> {
        return try {
            val response = api.getStats()
            Result.success(DocumentStats(response.totalDocuments, response.totalChunks))
        } catch (e: HttpException) {
            Result.failure(Exception("Server error (${e.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }

    override suspend fun getDocumentList(): Result<List<DocumentListItem>> {
        return try {
            val response = api.getDocumentList()
            Result.success(
                response.map {
                    DocumentListItem(
                        filename = it.filename,
                        chunks = it.chunks,
                        uploadedAt = it.uploadedAt?.let { seconds -> (seconds * 1000).toLong() }
                    )
                }
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Server error (${e.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }

    override suspend fun deleteDocument(filename: String): Result<Unit> {
        return try {
            api.deleteDocument(filename)
            Result.success(Unit)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                403 -> "You don't have permission to delete documents"
                404 -> "Document not found"
                else -> "Server error (${e.code()})"
            }
            Result.failure(Exception(message))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }
}