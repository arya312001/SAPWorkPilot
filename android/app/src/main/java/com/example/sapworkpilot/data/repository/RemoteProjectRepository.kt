package com.example.sapworkpilot.data.repository

import com.example.sapworkpilot.data.remote.ProjectApi
import com.example.sapworkpilot.data.remote.dto.HeatmapAreaDto
import com.example.sapworkpilot.data.remote.dto.RiskDto
import com.example.sapworkpilot.data.remote.dto.TicketDto
import com.example.sapworkpilot.domain.model.ChatMessage
import com.example.sapworkpilot.domain.model.HeatmapArea
import com.example.sapworkpilot.domain.model.JiraTicket
import com.example.sapworkpilot.domain.model.MeetingSummary
import com.example.sapworkpilot.domain.model.Milestone
import com.example.sapworkpilot.domain.model.Project
import com.example.sapworkpilot.domain.model.Risk
import com.example.sapworkpilot.domain.model.RiskLevel
import com.example.sapworkpilot.domain.model.TicketStatus
import com.example.sapworkpilot.domain.repository.ProjectRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RemoteProjectRepository @Inject constructor(
    private val api: ProjectApi,
    private val fake: FakeProjectRepository
) : ProjectRepository {

    // Temporary: if the backend fails, fall back to the fake data so the app does not crash.
    private suspend fun <T> withFallback(remote: suspend () -> T, fallback: suspend () -> T): T =
        try {
            remote()
        } catch (e: IOException) {
            fallback()
        } catch (e: HttpException) {
            fallback()
        }

    override suspend fun getProjects(): List<Project> = withFallback(
        remote = {
            api.getDashboard().projects.map {
                Project(it.id, it.name, it.description, it.status.toRiskLevel())
            }
        },
        fallback = { fake.getProjects() }
    )

    override suspend fun getHeatmap(): List<HeatmapArea> = withFallback(
        remote = { api.getHeatmap().map { it.toDomain() } },
        fallback = { fake.getHeatmap() }
    )

    override suspend fun getRisks(): List<Risk> = withFallback(
        remote = { api.getRisks().map { it.toDomain() } },
        fallback = { fake.getRisks() }
    )

    override suspend fun getMilestones(): List<Milestone> = withFallback(
        remote = { api.getDashboard().milestones.map { Milestone(it.title, it.date) } },
        fallback = { fake.getMilestones() }
    )

    override suspend fun getAllTickets(): List<JiraTicket> = withFallback(
        remote = { api.getHeatmap().flatMap { it.tickets }.map { it.toDomain() } },
        fallback = { fake.getAllTickets() }
    )

    override suspend fun getAiSummary(): String = withFallback(
        remote = { api.getDashboard().aiSummary },
        fallback = { fake.getAiSummary() }
    )

    // Not served by the backend yet
    override suspend fun getChatMessages(): List<ChatMessage> = fake.getChatMessages()

    override suspend fun getMeetingSummary(): MeetingSummary = fake.getMeetingSummary()
}

private fun String.toRiskLevel(): RiskLevel =
    RiskLevel.entries.firstOrNull { it.name == this } ?: RiskLevel.LOW

private fun String.toTicketStatus(): TicketStatus =
    TicketStatus.entries.firstOrNull { it.name == this } ?: TicketStatus.TO_DO

private fun TicketDto.toDomain() = JiraTicket(
    key = key,
    title = title,
    assignee = assignee,
    status = status.toTicketStatus(),
    priority = priority,
    isOverdue = isOverdue
)

private fun HeatmapAreaDto.toDomain() = HeatmapArea(
    name = name,
    level = level.toRiskLevel(),
    explanation = explanation,
    tickets = tickets.map { it.toDomain() }
)

private fun RiskDto.toDomain() = Risk(
    id = id,
    title = title,
    area = area,
    level = level.toRiskLevel(),
    reason = reason,
    source = source
)