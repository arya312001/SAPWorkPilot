package com.arya.sapworkpilot.domain.repository

import com.arya.sapworkpilot.domain.model.ChatMessage

interface ChatRepository {
    suspend fun askQuestion(question: String): Result<ChatMessage>
}