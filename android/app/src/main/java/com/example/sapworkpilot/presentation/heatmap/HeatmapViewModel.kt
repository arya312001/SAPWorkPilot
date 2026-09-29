package com.example.sapworkpilot.presentation.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sapworkpilot.domain.model.HeatmapArea
import com.example.sapworkpilot.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HeatmapUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val areas: List<HeatmapArea> = emptyList(),
    val selected: HeatmapArea? = null,
    val lastUpdated: Long = 0L,
    val errorMessage: String? = null
)

@HiltViewModel
class HeatmapViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    init {
        load(isRefresh = false)
    }

    fun onRefresh() {
        if (_uiState.value.isRefreshing) return
        load(isRefresh = true)
    }

    fun onAreaSelected(area: HeatmapArea) {
        _uiState.value = _uiState.value.copy(selected = area)
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )
            try {
                val areas = repository.getHeatmap()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    areas = areas,
                    selected = null,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = e.message ?: "Failed to load heatmap data"
                )
            }
        }
    }
}