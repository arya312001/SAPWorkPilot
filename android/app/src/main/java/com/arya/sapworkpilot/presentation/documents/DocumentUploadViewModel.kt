package com.arya.sapworkpilot.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arya.sapworkpilot.domain.repository.DocumentListItem
import com.arya.sapworkpilot.domain.repository.DocumentRepository
import com.arya.sapworkpilot.domain.repository.DocumentStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DocumentSortOrder { NAME, NEWEST_FIRST }

data class DocumentUploadUiState(
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val stats: DocumentStats? = null,
    val documents: List<DocumentListItem> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: DocumentSortOrder = DocumentSortOrder.NEWEST_FIRST,
    val message: String? = null,
    val isError: Boolean = false
) {
    val filteredDocuments: List<DocumentListItem>
        get() {
            val filtered = if (searchQuery.isBlank()) {
                documents
            } else {
                documents.filter { it.filename.contains(searchQuery, ignoreCase = true) }
            }
            return when (sortOrder) {
                DocumentSortOrder.NAME -> filtered.sortedBy { it.filename.lowercase() }
                DocumentSortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.uploadedAt ?: 0L }
            }
        }
}

@HiltViewModel
class DocumentUploadViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUploadUiState())
    val uiState: StateFlow<DocumentUploadUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            val statsResult = repository.getStats()
            val listResult = repository.getDocumentList()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = statsResult.getOrNull(),
                documents = listResult.getOrNull() ?: emptyList()
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSortOrderChange(order: DocumentSortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
    }

    fun onFileSelected(fileName: String, fileBytes: ByteArray) {
        if (_uiState.value.isUploading) return
        _uiState.value = _uiState.value.copy(isUploading = true, message = null)

        viewModelScope.launch {
            val result = repository.uploadDocument(fileName, fileBytes)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        message = "${it.filename} added (${it.chunksAdded} chunks)",
                        isError = false
                    )
                    refresh()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        message = it.message ?: "Upload failed",
                        isError = true
                    )
                }
            )
        }
    }

    fun onDeleteDocument(filename: String) {
        viewModelScope.launch {
            val result = repository.deleteDocument(filename)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        message = "$filename removed",
                        isError = false
                    )
                    refresh()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        message = it.message ?: "Delete failed",
                        isError = true
                    )
                }
            )
        }
    }
}