package com.example.sapworkpilot.domain.repository

import com.example.sapworkpilot.domain.model.MeetingSummary

interface MeetingRepository {
    suspend fun summarize(title: String, notes: String): Result<MeetingSummary>
}