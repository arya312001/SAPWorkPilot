package com.example.sapworkpilot.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.pm.PackageManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("< Back") }

        Text(
            text = "Backend Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Enter your laptop's current IP address and port. Find it with 'ipconfig' on the laptop.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = state.input,
            onValueChange = viewModel::onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Backend URL") },
            placeholder = { Text("http://192.168.10.51:8000/") },
            singleLine = true
        )

        OutlinedButton(
            onClick = viewModel::onTestConnection,
            enabled = !state.isTesting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isTesting) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text("Testing...")
            } else {
                Text("Test Connection")
            }
        }

        state.testResult?.let {
            Text(
                text = it,
                color = if (state.testSucceeded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(onClick = viewModel::onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }

        state.savedMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "Current: ${state.currentUrl}",
            style = MaterialTheme.typography.labelSmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Backend Status", fontWeight = FontWeight.SemiBold)
                StatusRow("AI Chat (LLM)", state.llmConfigured)
                StatusRow("Jira Integration", state.jiraConfigured)
            }
        }

        Text(
            text = "App version: $appVersion",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatusRow(label: String, configured: Boolean?) {
    val text = when (configured) {
        true -> "Configured"
        false -> "Not configured"
        null -> "Unknown"
    }
    Text(
        text = "$label: $text",
        style = MaterialTheme.typography.bodySmall
    )
}