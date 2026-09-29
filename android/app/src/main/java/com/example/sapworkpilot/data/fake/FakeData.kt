package com.example.sapworkpilot.data.fake

import com.example.sapworkpilot.domain.model.ChatMessage
import com.example.sapworkpilot.domain.model.HeatmapArea
import com.example.sapworkpilot.domain.model.JiraTicket
import com.example.sapworkpilot.domain.model.MeetingSummary
import com.example.sapworkpilot.domain.model.Milestone
import com.example.sapworkpilot.domain.model.Project
import com.example.sapworkpilot.domain.model.Risk
import com.example.sapworkpilot.domain.model.RiskLevel
import com.example.sapworkpilot.domain.model.TicketStatus

object FakeData {

    val projects = listOf(
        Project("p1", "S/4HANA Finance Migration", "Migrating legacy ERP finance to S/4HANA", RiskLevel.HIGH),
        Project("p2", "SAP Integration Modernization", "Moving interfaces to SAP BTP Integration Suite", RiskLevel.MEDIUM),
        Project("p3", "Warehouse Rollout", "Extended Warehouse Management deployment", RiskLevel.LOW)
    )

    private val financeTickets = listOf(
        JiraTicket("FIN-101", "GL account mapping incomplete", "Rahul", TicketStatus.BLOCKED, "Critical", true),
        JiraTicket("FIN-108", "Currency conversion rules review", "Priya", TicketStatus.IN_PROGRESS, "High", false)
    )

    private val dataTickets = listOf(
        JiraTicket("DM-45", "Legacy vendor data cleansing", "Amit", TicketStatus.BLOCKED, "Critical", true),
        JiraTicket("DM-52", "Migration cockpit dry run", "Sneha", TicketStatus.IN_PROGRESS, "High", false)
    )

    private val integrationTickets = listOf(
        JiraTicket("INT-23", "Bank interface testing", "Karan", TicketStatus.IN_PROGRESS, "Medium", false)
    )

    private val testingTickets = listOf(
        JiraTicket("QA-77", "UAT scripts for finance close", "Neha", TicketStatus.TO_DO, "Medium", false)
    )

    val heatmap = listOf(
        HeatmapArea(
            "Finance", RiskLevel.HIGH,
            "GL account mapping is blocked and overdue, which delays the finance close scope.",
            financeTickets
        ),
        HeatmapArea(
            "Data Migration", RiskLevel.HIGH,
            "Vendor data cleansing is blocked and the dry run depends on it.",
            dataTickets
        ),
        HeatmapArea(
            "Integration", RiskLevel.MEDIUM,
            "Bank interface testing is progressing but has a tight deadline.",
            integrationTickets
        ),
        HeatmapArea(
            "Testing", RiskLevel.MEDIUM,
            "UAT scripts have not started and depend on migration completion.",
            testingTickets
        ),
        HeatmapArea(
            "Deployment", RiskLevel.LOW,
            "Cutover plan is drafted and on schedule.",
            emptyList()
        ),
        HeatmapArea(
            "Security", RiskLevel.LOW,
            "Role design is complete and under review.",
            emptyList()
        )
    )

    val risks = listOf(
        Risk("r1", "GL mapping delay", "Finance", RiskLevel.HIGH,
            "FIN-101 is blocked and overdue, and it affects the finance close.", "Jira FIN-101"),
        Risk("r2", "Vendor data quality", "Data Migration", RiskLevel.HIGH,
            "DM-45 is blocked, so the migration dry run may slip.", "Jira DM-45"),
        Risk("r3", "UAT start dependency", "Testing", RiskLevel.MEDIUM,
            "UAT cannot start until the migration dry run is done.", "FURY Methodology.pdf"),
        Risk("r4", "Bank interface deadline", "Integration", RiskLevel.MEDIUM,
            "Testing window is tight before the cutover date.", "Project Plan v3.docx")
    )

    val milestones = listOf(
        Milestone("Data migration dry run", "15 Oct"),
        Milestone("UAT start", "01 Nov"),
        Milestone("Go-live cutover", "15 Dec")
    )

    val allTickets = financeTickets + dataTickets + integrationTickets + testingTickets

    val chatMessages = listOf(
        ChatMessage("m1", "Which critical tickets are blocked?", true),
        ChatMessage(
            "m2",
            "Two critical tickets are blocked: FIN-101 (GL account mapping) and DM-45 (vendor data cleansing). Both are overdue.",
            false,
            listOf("Jira FIN-101", "Jira DM-45")
        )
    )

    val meetingSummary = MeetingSummary(
        title = "Weekly Finance Migration Sync",
        summary = "Team reviewed blockers in GL mapping and vendor data cleansing.",
        decisions = listOf("Escalate FIN-101 to the finance lead", "Freeze scope for the dry run"),
        actionItems = listOf("Rahul: finish GL mapping by Friday", "Amit: share cleansed vendor file by Monday")
    )
}