package com.example.sapworkpilot.data.remote.dto

data class ChatRequestDto(
    val question: String
)

data class ChatResponseDto(
    val answer: String,
    val sources: List<String>
)