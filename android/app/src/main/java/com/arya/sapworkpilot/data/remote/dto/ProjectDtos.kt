package com.arya.sapworkpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProjectDto(
    val id: String,
    val name: String,
    val description: String,
    val status: String
)

data class MilestoneDto(
    val title: String,
    val date: String
)

data class TicketDto(
    val key: String,
    val title: String,
    val assignee: String,
    val status: String,
    val priority: String,
    @SerializedName("is_overdue") val isOverdue: Boolean
)

data class HeatmapAreaDto(
    val name: String,
    val level: String,
    val explanation: String,
    val tickets: List<TicketDto>
)

data class RiskDto(
    val id: String,
    val title: String,
    val area: String,
    val level: String,
    val reason: String,
    val source: String
)

data class DashboardDto(
    val projects: List<ProjectDto>,
    val milestones: List<MilestoneDto>,
    @SerializedName("ai_summary") val aiSummary: String,
    @SerializedName("high_risk_count") val highRiskCount: Int,
    @SerializedName("blocked_count") val blockedCount: Int,
    @SerializedName("overdue_count") val overdueCount: Int
)