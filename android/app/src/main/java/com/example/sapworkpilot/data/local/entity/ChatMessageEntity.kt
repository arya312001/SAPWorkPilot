package com.example.sapworkpilot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val text: String,
    val isUser: Boolean,
    val sources: String, // comma-separated, since Room needs simple column types
    val timestamp: Long
)