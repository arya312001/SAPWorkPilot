package com.example.sapworkpilot.domain.repository

import com.example.sapworkpilot.domain.model.ChatMessage

interface ChatRepository {
    suspend fun askQuestion(question: String): Result<ChatMessage>
}