package com.example.sapworkpilot.presentation.documents

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.example.sapworkpilot.domain.repository.DocumentListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentUploadScreen(
    viewModel: DocumentUploadViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pendingDeleteFilename by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: "document"

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) {
            viewModel.onFileSelected(name, bytes)
        }
    }

    pendingDeleteFilename?.let { filename ->
        AlertDialog(
            onDismissRequest = { pendingDeleteFilename = null },
            title = { Text("Delete document?") },
            text = { Text("\"$filename\" and its indexed content will be permanently removed. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteDocument(filename)
                    pendingDeleteFilename = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteFilename = null }) { Text("Cancel") }
            }
        )
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
            text = "Project Documents",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Upload SAP documentation, project plans, or notes. The AI Chat will use these to answer your questions.",
            style = MaterialTheme.typography.bodySmall
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Documents indexed: ${state.stats?.totalDocuments ?: 0}")
                    Text("Total chunks: ${state.stats?.totalChunks ?: 0}")
                }
            }
        }

        Button(
            onClick = {
                filePicker.launch(
                    arrayOf(
                        "application/pdf",
                        "text/plain",
                        "text/markdown",
                        "text/csv",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                )
            },
            enabled = !state.isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isUploading) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text("Uploading...")
            } else {
                Text("Choose a file (PDF, DOCX, XLSX, CSV, TXT)")
            }
        }

        state.message?.let {
            Text(
                text = it,
                color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!state.isLoading) {
            if (state.documents.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Uploaded documents", fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = {
                        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val text = state.documents.joinToString("\n") { doc ->
                            val dateText = doc.uploadedAt?.let { formatter.format(Date(it)) } ?: "unknown date"
                            "${doc.filename} - ${doc.chunks} chunks - $dateText"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share document list"))
                    }) {
                        Text("Share")
                    }
                }
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search documents...") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.sortOrder == DocumentSortOrder.NEWEST_FIRST,
                        onClick = { viewModel.onSortOrderChange(DocumentSortOrder.NEWEST_FIRST) },
                        label = { Text("Newest") }
                    )
                    FilterChip(
                        selected = state.sortOrder == DocumentSortOrder.NAME,
                        onClick = { viewModel.onSortOrderChange(DocumentSortOrder.NAME) },
                        label = { Text("Name") }
                    )
                }
                if (state.filteredDocuments.isEmpty()) {
                    Text(
                        text = "No documents match \"${state.searchQuery}\".",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.filteredDocuments) { doc ->
                            DocumentRow(doc, onDelete = { pendingDeleteFilename = doc.filename })
                        }
                    }
                }
            } else {
                Text(
                    text = "No documents uploaded yet. Add one above to power AI Chat answers.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DocumentRow(doc: DocumentListItem, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(doc.filename, style = MaterialTheme.typography.bodyMedium)
                val dateText = doc.uploadedAt?.let {
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(it))
                }
                Text(
                    text = "${doc.chunks} chunks" + (dateText?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}