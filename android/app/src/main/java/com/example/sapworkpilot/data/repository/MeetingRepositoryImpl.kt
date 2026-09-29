package com.example.sapworkpilot.data.repository

import com.example.sapworkpilot.data.remote.MeetingApi
import com.example.sapworkpilot.data.remote.dto.MeetingNotesRequestDto
import com.example.sapworkpilot.domain.model.MeetingSummary
import com.example.sapworkpilot.domain.repository.MeetingRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MeetingRepositoryImpl @Inject constructor(
    private val api: MeetingApi
) : MeetingRepository {

    override suspend fun summarize(title: String, notes: String): Result<MeetingSummary> {
        return try {
            val response = api.summarize(MeetingNotesRequestDto(title, notes))
            Result.success(
                MeetingSummary(
                    title = response.title,
                    summary = response.summary,
                    decisions = response.decisions,
                    actionItems = response.actionItems
                )
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Server error (${e.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }
}