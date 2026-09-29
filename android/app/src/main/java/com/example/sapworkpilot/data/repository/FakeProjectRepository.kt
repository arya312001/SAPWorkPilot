package com.example.sapworkpilot.data.repository

import com.example.sapworkpilot.data.fake.FakeData
import com.example.sapworkpilot.domain.model.ChatMessage
import com.example.sapworkpilot.domain.model.HeatmapArea
import com.example.sapworkpilot.domain.model.JiraTicket
import com.example.sapworkpilot.domain.model.MeetingSummary
import com.example.sapworkpilot.domain.model.Milestone
import com.example.sapworkpilot.domain.model.Project
import com.example.sapworkpilot.domain.model.Risk
import com.example.sapworkpilot.domain.repository.ProjectRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeProjectRepository @Inject constructor() : ProjectRepository {

    override suspend fun getProjects(): List<Project> {
        delay(300)
        return FakeData.projects
    }

    override suspend fun getHeatmap(): List<HeatmapArea> {
        delay(300)
        return FakeData.heatmap
    }

    override suspend fun getRisks(): List<Risk> {
        delay(300)
        return FakeData.risks
    }

    override suspend fun getMilestones(): List<Milestone> = FakeData.milestones

    override suspend fun getAllTickets(): List<JiraTicket> = FakeData.allTickets

    override suspend fun getChatMessages(): List<ChatMessage> = FakeData.chatMessages

    override suspend fun getMeetingSummary(): MeetingSummary = FakeData.meetingSummary

    override suspend fun getAiSummary(): String =
        "Finance and Data Migration are at high risk because critical Jira tickets " +
                "(FIN-101, DM-45) are blocked and overdue. Resolve these first to protect the UAT start date."
}