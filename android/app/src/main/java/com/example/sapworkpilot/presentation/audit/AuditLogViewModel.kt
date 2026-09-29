package com.example.sapworkpilot.presentation.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sapworkpilot.domain.repository.AuditEntry
import com.example.sapworkpilot.domain.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuditLogUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val entries: List<AuditEntry> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val oldestFirst: Boolean = false
) {
    val filteredEntries: List<AuditEntry>
        get() {
            val filtered = if (searchQuery.isBlank()) {
                entries
            } else {
                entries.filter {
                    it.user.contains(searchQuery, ignoreCase = true) ||
                            it.action.contains(searchQuery, ignoreCase = true) ||
                            it.detail.contains(searchQuery, ignoreCase = true)
                }
            }
            return if (oldestFirst) filtered.sortedBy { it.timestamp } else filtered.sortedByDescending { it.timestamp }
        }
}

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditLogUiState())
    val uiState: StateFlow<AuditLogUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onToggleSortOrder() {
        _uiState.value = _uiState.value.copy(oldestFirst = !_uiState.value.oldestFirst)
    }

    fun onRefresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            val result = repository.getLog()
            _uiState.value = result.fold(
                onSuccess = { _uiState.value.copy(isRefreshing = false, entries = it) },
                onFailure = { _uiState.value.copy(isRefreshing = false, errorMessage = it.message) }
            )
        }
    }

    fun buildExportText(): String {
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return _uiState.value.filteredEntries.joinToString("\n") { entry ->
            val date = formatter.format(java.util.Date(entry.timestamp))
            "$date | ${entry.user} | ${entry.action} | ${entry.detail}"
        }
    }

    init {
        viewModelScope.launch {
            val result = repository.getLog()
            _uiState.value = result.fold(
                onSuccess = { AuditLogUiState(isLoading = false, entries = it) },
                onFailure = { AuditLogUiState(isLoading = false, errorMessage = it.message) }
            )
        }
    }
}