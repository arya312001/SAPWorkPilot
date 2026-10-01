package com.arya.sapworkpilot.data.remote

import com.arya.sapworkpilot.data.remote.dto.MeetingNotesRequestDto
import com.arya.sapworkpilot.data.remote.dto.MeetingSummaryDto
import retrofit2.http.Body
import retrofit2.http.POST

interface MeetingApi {

    @POST("meetings/summarize")
    suspend fun summarize(@Body request: MeetingNotesRequestDto): MeetingSummaryDto
}