package com.arya.sapworkpilot.data.repository

import com.arya.sapworkpilot.domain.repository.AuditEntry
import com.arya.sapworkpilot.data.remote.AuditApi
import com.arya.sapworkpilot.domain.repository.AuditRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuditRepositoryImpl @Inject constructor(
    private val api: AuditApi
) : AuditRepository {

    override suspend fun getLog(): Result<List<AuditEntry>> {
        return try {
            val response = api.getLog()
            Result.success(
                response.map {
                    AuditEntry(
                        timestamp = (it.timestamp * 1000).toLong(),
                        user = it.user,
                        action = it.action,
                        detail = it.detail
                    )
                }
            )
        } catch (e: HttpException) {
            val message = if (e.code() == 403) "Only admins can view the audit log" else "Server error (${e.code()})"
            Result.failure(Exception(message))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }
}