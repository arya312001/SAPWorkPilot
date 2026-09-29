package com.example.sapworkpilot.data.repository

import com.example.sapworkpilot.data.remote.ChatApi
import com.example.sapworkpilot.data.remote.dto.ChatRequestDto
import com.example.sapworkpilot.domain.model.ChatMessage
import com.example.sapworkpilot.domain.repository.ChatRepository
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val api: ChatApi
) : ChatRepository {

    override suspend fun askQuestion(question: String): Result<ChatMessage> {
        return try {
            val response = api.ask(ChatRequestDto(question))
            Result.success(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.answer,
                    isUser = false,
                    sources = response.sources
                )
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Server error (${e.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }
}