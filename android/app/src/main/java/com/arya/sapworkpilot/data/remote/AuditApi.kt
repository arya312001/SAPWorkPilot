package com.arya.sapworkpilot.data.remote

import com.arya.sapworkpilot.data.remote.dto.AuditEntryDto
import retrofit2.http.GET

interface AuditApi {

    @GET("audit/log")
    suspend fun getLog(): List<AuditEntryDto>
}