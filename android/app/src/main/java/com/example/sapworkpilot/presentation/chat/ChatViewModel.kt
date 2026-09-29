package com.example.sapworkpilot.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sapworkpilot.data.local.entity.ChatMessageDao
import com.example.sapworkpilot.data.local.entity.ChatMessageEntity
import com.example.sapworkpilot.domain.model.ChatMessage
import com.example.sapworkpilot.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatMessageDao: ChatMessageDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var lastFailedQuestion: String? = null

    init {
        viewModelScope.launch {
            chatMessageDao.observeAll().collect { entities ->
                _uiState.value = _uiState.value.copy(messages = entities.map { it.toDomain() })
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(input = text)
    }

    fun onClearHistory() {
        viewModelScope.launch {
            chatMessageDao.clearAll()
        }
    }

    fun onRetryLast() {
        val question = lastFailedQuestion ?: return
        _uiState.value = _uiState.value.copy(errorMessage = null, canRetry = false)
        sendQuestion(question, saveUserMessage = false)
    }

    fun onSend() {
        val question = _uiState.value.input.trim()
        if (question.isEmpty() || _uiState.value.isSending) return
        _uiState.value = _uiState.value.copy(input = "", errorMessage = null, canRetry = false)
        sendQuestion(question, saveUserMessage = true)
    }

    private fun sendQuestion(question: String, saveUserMessage: Boolean) {
        _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null, canRetry = false)

        viewModelScope.launch {
            if (saveUserMessage) {
                val userMessage = ChatMessage(UUID.randomUUID().toString(), question, true)
                chatMessageDao.insert(userMessage.toEntity())
            }

            val result = chatRepository.askQuestion(question)
            result.fold(
                onSuccess = { reply ->
                    lastFailedQuestion = null
                    chatMessageDao.insert(reply.toEntity())
                    _uiState.value = _uiState.value.copy(isSending = false)
                },
                onFailure = { error ->
                    lastFailedQuestion = question
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = error.message ?: "Something went wrong.",
                        canRetry = true
                    )
                }
            )
        }
    }
}

private fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    text = text,
    isUser = isUser,
    sources = sources.joinToString(","),
    timestamp = timestamp
)

private fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    text = text,
    isUser = isUser,
    sources = if (sources.isBlank()) emptyList() else sources.split(","),
    timestamp = timestamp
)