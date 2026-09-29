package com.example.sapworkpilot.presentation.theme

import androidx.compose.ui.graphics.Color
import com.example.sapworkpilot.domain.model.RiskLevel
import com.example.sapworkpilot.domain.model.TicketStatus

val GreenLow = Color(0xFF2E7D32)
val AmberMedium = Color(0xFFF9A825)
val RedHigh = Color(0xFFC62828)

fun RiskLevel.color(): Color = when (this) {
    RiskLevel.LOW -> GreenLow
    RiskLevel.MEDIUM -> AmberMedium
    RiskLevel.HIGH -> RedHigh
}

fun RiskLevel.label(): String = when (this) {
    RiskLevel.LOW -> "Low Risk"
    RiskLevel.MEDIUM -> "Needs Attention"
    RiskLevel.HIGH -> "Critical"
}

fun TicketStatus.label(): String = when (this) {
    TicketStatus.TO_DO -> "To Do"
    TicketStatus.IN_PROGRESS -> "In Progress"
    TicketStatus.BLOCKED -> "Blocked"
    TicketStatus.DONE -> "Done"
}