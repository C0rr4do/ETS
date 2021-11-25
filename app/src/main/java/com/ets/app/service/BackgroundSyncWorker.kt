package com.ets.app.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncService: SyncService,
    private val notificationService: NotificationService
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            notificationService.notifyBackgroundSyncRunning()
            syncService.downloadLatestPlan()
            Result.success()
        } catch (_: Exception) {
            notificationService.hideBackgroundSyncRunning()
            Result.failure()
        }
    }
}