package com.example.sapworkpilot.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.sapworkpilot.domain.model.Project
import com.example.sapworkpilot.domain.model.RiskLevel
import com.example.sapworkpilot.presentation.theme.color
import com.example.sapworkpilot.presentation.theme.label
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    canManageDocuments: Boolean = true,
    isAdmin: Boolean = false,
    userName: String? = null,
    onOpenHeatmap: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenRisks: () -> Unit = {},
    onOpenMeetings: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAuditLog: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Executive Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        userName?.let {
                            Text(
                                text = "Welcome, $it",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            }

            if (state.isOffline) {
                item {
                    Text(
                        text = "Showing cached data — backend unreachable",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.lastUpdated > 0) {
                item {
                    val timeText = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(state.lastUpdated))
                    Text(
                        text = "Last updated: $timeText",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("High Risk", state.highRiskCount.toString(), RiskLevel.HIGH.color(), Modifier.weight(1f))
                    StatCard("Blocked", state.blockedCount.toString(), RiskLevel.MEDIUM.color(), Modifier.weight(1f))
                    StatCard("Overdue", state.overdueCount.toString(), RiskLevel.HIGH.color(), Modifier.weight(1f))
                }
            }

            item { SectionTitle("AI Summary") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = state.aiSummary,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item { SectionTitle("Projects") }
            items(state.projects) { project -> ProjectRow(project) }

            item { SectionTitle("Upcoming Milestones") }
            items(state.milestones) { milestone ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(milestone.title, style = MaterialTheme.typography.bodyLarge)
                        Text(milestone.date, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { SectionTitle("Quick Actions") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onOpenChat, modifier = Modifier.weight(1f)) { Text("AI Chat") }
                        Button(onClick = onOpenHeatmap, modifier = Modifier.weight(1f)) { Text("Heatmap") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenRisks, modifier = Modifier.weight(1f)) { Text("Risks") }
                        OutlinedButton(onClick = onOpenMeetings, modifier = Modifier.weight(1f)) { Text("Meetings") }
                    }
                    if (canManageDocuments) {
                        OutlinedButton(onClick = onOpenDocuments, modifier = Modifier.fillMaxWidth()) {
                            Text("Documents")
                        }
                    }
                    OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                        Text("Settings")
                    }
                    if (isAdmin) {
                        OutlinedButton(onClick = onOpenAuditLog, modifier = Modifier.fillMaxWidth()) {
                            Text("Audit Log")
                        }
                    }
                    OutlinedButton(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
                        Text("Profile")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ProjectRow(project: Project) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(project.status.color(), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(project.description, style = MaterialTheme.typography.bodySmall)
            }
            Text(project.status.label(), style = MaterialTheme.typography.labelMedium)
        }
    }
}