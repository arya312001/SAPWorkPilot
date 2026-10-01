package com.arya.sapworkpilot.domain.repository

import com.arya.sapworkpilot.domain.model.MeetingSummary

interface MeetingRepository {
    suspend fun summarize(title: String, notes: String): Result<MeetingSummary>
}