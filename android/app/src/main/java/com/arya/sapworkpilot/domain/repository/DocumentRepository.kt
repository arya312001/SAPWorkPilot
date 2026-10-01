package com.arya.sapworkpilot.domain.repository

data class DocumentStats(val totalDocuments: Int, val totalChunks: Int)
data class UploadResult(val filename: String, val chunksAdded: Int, val stats: DocumentStats)
data class DocumentListItem(val filename: String, val chunks: Int, val uploadedAt: Long?)

interface DocumentRepository {
    suspend fun uploadDocument(fileName: String, fileBytes: ByteArray): Result<UploadResult>
    suspend fun getStats(): Result<DocumentStats>
    suspend fun getDocumentList(): Result<List<DocumentListItem>>
    suspend fun deleteDocument(filename: String): Result<Unit>
}