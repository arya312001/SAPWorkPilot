package com.arya.sapworkpilot.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arya.sapworkpilot.domain.repository.ProjectRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DashboardSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val projectRepository: ProjectRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            projectRepository.getHeatmap()
            projectRepository.getAllTickets()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}