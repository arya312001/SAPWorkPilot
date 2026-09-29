package com.example.sapworkpilot.data.remote

import com.example.sapworkpilot.data.remote.dto.DashboardDto
import com.example.sapworkpilot.data.remote.dto.HeatmapAreaDto
import com.example.sapworkpilot.data.remote.dto.RiskDto
import retrofit2.http.GET

interface ProjectApi {

    @GET("projects/dashboard")
    suspend fun getDashboard(): DashboardDto

    @GET("projects/heatmap")
    suspend fun getHeatmap(): List<HeatmapAreaDto>

    @GET("projects/risks")
    suspend fun getRisks(): List<RiskDto>
}