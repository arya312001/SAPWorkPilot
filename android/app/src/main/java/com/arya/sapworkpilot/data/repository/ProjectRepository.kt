package com.arya.sapworkpilot.domain.repository

import com.arya.sapworkpilot.domain.model.ChatMessage
import com.arya.sapworkpilot.domain.model.HeatmapArea
import com.arya.sapworkpilot.domain.model.JiraTicket
import com.arya.sapworkpilot.domain.model.MeetingSummary
import com.arya.sapworkpilot.domain.model.Milestone
import com.arya.sapworkpilot.domain.model.Project
import com.arya.sapworkpilot.domain.model.Risk

interface ProjectRepository {
    suspend fun getProjects(): List<Project>
    suspend fun getHeatmap(): List<HeatmapArea>
    suspend fun getRisks(): List<Risk>
    suspend fun getMilestones(): List<Milestone>
    suspend fun getAllTickets(): List<JiraTicket>
    suspend fun getChatMessages(): List<ChatMessage>
    suspend fun getMeetingSummary(): MeetingSummary
    suspend fun getAiSummary(): String
}