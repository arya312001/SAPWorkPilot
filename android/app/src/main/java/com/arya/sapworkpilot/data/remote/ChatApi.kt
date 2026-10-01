package com.arya.sapworkpilot.data.remote

import com.arya.sapworkpilot.data.remote.dto.ChatRequestDto
import com.arya.sapworkpilot.data.remote.dto.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {

    @POST("chat/ask")
    suspend fun ask(@Body request: ChatRequestDto): ChatResponseDto
}