package com.arya.sapworkpilot.domain.repository

data class AuditEntry(val timestamp: Long, val user: String, val action: String, val detail: String)

interface AuditRepository {
    suspend fun getLog(): Result<List<AuditEntry>>
}