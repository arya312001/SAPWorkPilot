package com.example.sapworkpilot.domain.model

enum class RiskLevel { LOW, MEDIUM, HIGH }

enum class TicketStatus { TO_DO, IN_PROGRESS, BLOCKED, DONE }

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val status: RiskLevel
)

data class JiraTicket(
    val key: String,
    val title: String,
    val assignee: String,
    val status: TicketStatus,
    val priority: String,
    val isOverdue: Boolean
)

data class HeatmapArea(
    val name: String,
    val level: RiskLevel,
    val explanation: String,
    val tickets: List<JiraTicket>
)

data class Risk(
    val id: String,
    val title: String,
    val area: String,
    val level: RiskLevel,
    val reason: String,
    val source: String
)

data class Milestone(
    val title: String,
    val date: String
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val sources: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class MeetingSummary(
    val title: String,
    val summary: String,
    val decisions: List<String>,
    val actionItems: List<String>
)