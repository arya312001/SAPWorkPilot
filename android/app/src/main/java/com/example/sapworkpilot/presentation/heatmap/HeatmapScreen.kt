package com.example.sapworkpilot.presentation.heatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sapworkpilot.domain.model.HeatmapArea
import com.example.sapworkpilot.domain.model.JiraTicket
import com.example.sapworkpilot.presentation.theme.color
import com.example.sapworkpilot.presentation.theme.label
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(
    viewModel: HeatmapViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) { Text("< Back") }

            if (state.lastUpdated > 0) {
                val timeText = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(state.lastUpdated))
                Text(
                    text = "Last updated: $timeText",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                text = "Project Heatmap",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Green = on track, Amber = needs attention, Red = critical. Tap an area to see details.",
                style = MaterialTheme.typography.bodySmall
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                state.areas.chunked(2).forEach { rowAreas ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowAreas.forEach { area ->
                            AreaTile(
                                area = area,
                                isSelected = state.selected == area,
                                onClick = { viewModel.onAreaSelected(area) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowAreas.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                state.selected?.let { AreaDetail(it) }
            }
        }
    }
}

@Composable
private fun AreaTile(
    area: HeatmapArea,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = area.level.color()),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = area.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = area.level.label(),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AreaDetail(area: HeatmapArea) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${area.name}: ${area.level.label()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = area.level.color()
            )
            Text("Why?", fontWeight = FontWeight.SemiBold)
            Text(area.explanation, style = MaterialTheme.typography.bodyMedium)

            Text("Related Jira tickets", fontWeight = FontWeight.SemiBold)
            if (area.tickets.isEmpty()) {
                Text("No open tickets.", style = MaterialTheme.typography.bodySmall)
            } else {
                area.tickets.forEach { TicketRow(it) }
            }
        }
    }
}

@Composable
private fun TicketRow(ticket: JiraTicket) {
    Column {
        Text(
            text = "${ticket.key}: ${ticket.title}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${ticket.status.label()} | ${ticket.priority} | ${ticket.assignee}" +
                    if (ticket.isOverdue) " | Overdue" else "",
            style = MaterialTheme.typography.bodySmall
        )
    }
}