package com.example.sapworkpilot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meeting_summaries")
data class MeetingSummaryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val decisions: String,   // stored as one item per line
    val actionItems: String, // stored as one item per line
    val createdAt: Long
)