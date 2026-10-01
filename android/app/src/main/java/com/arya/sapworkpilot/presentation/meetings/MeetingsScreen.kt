package com.arya.sapworkpilot.presentation.meetings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arya.sapworkpilot.data.local.entity.MeetingSummaryEntity
import com.arya.sapworkpilot.domain.model.MeetingSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeetingsScreen(
    viewModel: MeetingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete this summary?") },
            text = { Text("This meeting summary will be permanently removed. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteHistoryItem(id)
                    pendingDeleteId = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("< Back") }

        Text(
            text = "Meeting Notes Intelligence",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text("Meeting title (optional)", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = state.meetingTitle,
            onValueChange = viewModel::onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Weekly Finance Sync") },
            singleLine = true
        )

        Text("Paste meeting notes or a transcript", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = state.notes,
            onValueChange = { if (it.length <= 20000) viewModel.onNotesChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type or paste notes here...") },
            minLines = 4,
            maxLines = 8,
            supportingText = {
                if (state.notes.length > 18000) {
                    Text("${state.notes.length}/20000")
                }
            }
        )
        Button(
            onClick = viewModel::onGenerateSummary,
            enabled = state.notes.isNotBlank() && !state.isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isGenerating) "Generating..." else "Generate Summary")
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        state.meeting?.let {
            Text("Latest Summary", fontWeight = FontWeight.SemiBold)
            MeetingCard(it)
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.history.isNotEmpty()) {
            Text("History", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = state.historySearchQuery,
                onValueChange = viewModel::onHistorySearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search summaries...") },
                singleLine = true
            )
            if (state.filteredHistory.isEmpty()) {
                Text(
                    text = "No summaries match \"${state.historySearchQuery}\".",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                state.filteredHistory.forEach { entry ->
                    HistoryRow(
                        entry = entry,
                        onDelete = { pendingDeleteId = entry.id },
                        onShare = {
                            val text = buildShareText(entry)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share summary"))
                        }
                    )
                }
            }
        } else {
            Text(
                text = "No meeting summaries yet. Generate one above to see it here.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MeetingCard(meeting: MeetingSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text("Summary", fontWeight = FontWeight.SemiBold)
            Text(meeting.summary, style = MaterialTheme.typography.bodyMedium)

            Text("Decisions", fontWeight = FontWeight.SemiBold)
            meeting.decisions.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Action items", fontWeight = FontWeight.SemiBold)
            meeting.actionItems.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun buildShareText(entry: MeetingSummaryEntity): String {
    val decisionsText = entry.decisions.split("\n").filter { it.isNotBlank() }.joinToString("\n") { "- $it" }
    val actionsText = entry.actionItems.split("\n").filter { it.isNotBlank() }.joinToString("\n") { "- $it" }
    return buildString {
        appendLine("Meeting Summary")
        appendLine(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(entry.createdAt)))
        appendLine()
        appendLine(entry.summary)
        if (decisionsText.isNotBlank()) {
            appendLine()
            appendLine("Decisions:")
            appendLine(decisionsText)
        }
        if (actionsText.isNotBlank()) {
            appendLine()
            appendLine("Action items:")
            appendLine(actionsText)
        }
    }
}

@Composable
private fun HistoryRow(entry: MeetingSummaryEntity, onDelete: () -> Unit, onShare: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title.ifBlank { "Meeting Summary" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(entry.createdAt)),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Row {
                    TextButton(onClick = { isExpanded = !isExpanded }) {
                        Text(if (isExpanded) "Collapse" else "Expand")
                    }
                    TextButton(onClick = onShare) { Text("Share") }
                    TextButton(onClick = onDelete) { Text("Delete") }
                }
            }

            Text(
                text = entry.summary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2
            )

            if (isExpanded) {
                if (entry.decisions.isNotBlank()) {
                    Text(
                        text = "Decisions",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    entry.decisions.split("\n").forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (entry.actionItems.isNotBlank()) {
                    Text(
                        text = "Action items",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    entry.actionItems.split("\n").forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}