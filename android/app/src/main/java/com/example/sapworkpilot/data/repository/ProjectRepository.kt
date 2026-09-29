package com.example.sapworkpilot.domain.repository

import com.example.sapworkpilot.domain.model.ChatMessage
import com.example.sapworkpilot.domain.model.HeatmapArea
import com.example.sapworkpilot.domain.model.JiraTicket
import com.example.sapworkpilot.domain.model.MeetingSummary
import com.example.sapworkpilot.domain.model.Milestone
import com.example.sapworkpilot.domain.model.Project
import com.example.sapworkpilot.domain.model.Risk

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