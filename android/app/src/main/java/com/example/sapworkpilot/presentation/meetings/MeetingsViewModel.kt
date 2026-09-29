package com.example.sapworkpilot.presentation.meetings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sapworkpilot.data.local.entity.MeetingSummaryDao
import com.example.sapworkpilot.data.local.entity.MeetingSummaryEntity
import com.example.sapworkpilot.domain.model.MeetingSummary
import com.example.sapworkpilot.domain.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MeetingsUiState(
    val isLoading: Boolean = true,
    val meeting: MeetingSummary? = null,
    val meetingTitle: String = "",
    val notes: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val history: List<MeetingSummaryEntity> = emptyList(),
    val historySearchQuery: String = ""
) {
    val filteredHistory: List<MeetingSummaryEntity>
        get() = if (historySearchQuery.isBlank()) {
            history
        } else {
            history.filter { it.summary.contains(historySearchQuery, ignoreCase = true) }
        }
}

@HiltViewModel
class MeetingsViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val meetingSummaryDao: MeetingSummaryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingsUiState())
    val uiState: StateFlow<MeetingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            meetingSummaryDao.observeAll().collect { entities ->
                _uiState.value = _uiState.value.copy(isLoading = false, history = entities)
            }
        }
    }

    fun onHistorySearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(historySearchQuery = query)
    }

    fun onTitleChange(text: String) {
        _uiState.value = _uiState.value.copy(meetingTitle = text, errorMessage = null)
    }

    fun onNotesChange(text: String) {
        _uiState.value = _uiState.value.copy(notes = text, errorMessage = null)
    }

    fun onGenerateSummary() {
        val notes = _uiState.value.notes
        if (notes.isBlank() || _uiState.value.isGenerating) return

        val title = _uiState.value.meetingTitle.trim().ifBlank { "Meeting Summary" }
        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            val result = meetingRepository.summarize(title, notes)
            result.fold(
                onSuccess = { summary ->
                    meetingSummaryDao.insert(
                        MeetingSummaryEntity(
                            title = summary.title,
                            summary = summary.summary,
                            decisions = summary.decisions.joinToString("\n"),
                            actionItems = summary.actionItems.joinToString("\n"),
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        meeting = summary,
                        meetingTitle = "",
                        notes = ""
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = it.message
                    )
                }
            )
        }
    }

    fun onDeleteHistoryItem(id: String) {
        viewModelScope.launch {
            meetingSummaryDao.delete(id)
        }
    }
}