package com.example.sapworkpilot.presentation.risks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sapworkpilot.domain.model.Risk
import com.example.sapworkpilot.domain.model.RiskLevel
import com.example.sapworkpilot.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RisksUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allRisks: List<Risk> = emptyList(),
    val filter: RiskLevel? = null,
    val searchQuery: String = "",
    val lastUpdated: Long = 0L,
    val errorMessage: String? = null
) {
    val visibleRisks: List<Risk>
        get() {
            val byLevel = if (filter == null) allRisks else allRisks.filter { it.level == filter }
            return if (searchQuery.isBlank()) {
                byLevel
            } else {
                byLevel.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.area.contains(searchQuery, ignoreCase = true)
                }
            }
        }
}

@HiltViewModel
class RisksViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RisksUiState())
    val uiState: StateFlow<RisksUiState> = _uiState.asStateFlow()

    init {
        load(isRefresh = false)
    }

    fun onRefresh() {
        if (_uiState.value.isRefreshing) return
        load(isRefresh = true)
    }

    fun onFilterSelected(level: RiskLevel?) {
        _uiState.value = _uiState.value.copy(filter = level)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )
            try {
                val risks = repository.getRisks()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    allRisks = risks,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = e.message ?: "Failed to load risks"
                )
            }
        }
    }
}