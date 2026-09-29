package com.example.sapworkpilot.data.remote

import com.example.sapworkpilot.data.remote.dto.HealthDto
import retrofit2.http.GET

interface HealthApi {

    @GET("health")
    suspend fun getHealth(): HealthDto
}