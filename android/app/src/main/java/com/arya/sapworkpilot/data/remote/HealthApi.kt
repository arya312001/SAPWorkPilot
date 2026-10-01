package com.arya.sapworkpilot.data.remote

import com.arya.sapworkpilot.data.remote.dto.HealthDto
import retrofit2.http.GET

interface HealthApi {

    @GET("health")
    suspend fun getHealth(): HealthDto
}