package com.example.sapworkpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MeetingNotesRequestDto(
    val title: String,
    val notes: String
)

data class MeetingSummaryDto(
    val title: String,
    val summary: String,
    val decisions: List<String>,
    @SerializedName("action_items") val actionItems: List<String>
)