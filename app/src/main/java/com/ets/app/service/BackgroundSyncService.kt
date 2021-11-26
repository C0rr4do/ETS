package com.ets.app.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.*
import com.ets.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackgroundSyncService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefKeyBackgroundSyncTimes: String
        get() = context.resources.getString(R.string.pref_key_background_sync_times)

    private val prefKeyRunningBackgroundSyncTimes: String
        get() = context.resources.getString(R.string.pref_key_running_background_sync_times)

    private val sharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    private val enableBackgroundSync: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.resources.getString(R.string.pref_key_enable_background_sync), false
        )

    private suspend fun startDailyBackgroundSync(timestamp: Long): Boolean {
        val workRequest = generateDailyWork(timestamp)
        // Enqueue work
        val workName = nameFromTimestamp(timestamp)
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            ).await().also {
                PreferenceManager.getDefaultSharedPreferences(context)
                    .let { preferences ->
                        // Get names of work, that has been running so far
                        val runningNames = preferences.getStringSet(
                            prefKeyRunningBackgroundSyncTimes, mutableSetOf()
                        )!!.toMutableSet()

                        // Add the name of newly enqueued work
                        runningNames.add(workName)

                        // Save updated runningNames to preferences
                        preferences.edit().run {
                            putStringSet(prefKeyRunningBackgroundSyncTimes, runningNames)
                            apply()
                        }
                    }
                return true
            }
    }

    private suspend fun stopDailyBackgroundSync(timestamp: Long): Boolean {
        val workName = nameFromTimestamp(timestamp)
        WorkManager.getInstance(context)
            .cancelUniqueWork(workName).await().also {
                PreferenceManager.getDefaultSharedPreferences(context)
                    .let { preferences ->
                        // Get names of work, that has been running so far
                        val runningNames = preferences.getStringSet(
                            prefKeyRunningBackgroundSyncTimes, mutableSetOf()
                        )!!.toMutableSet()

                        // Remove the name of newly enqueued work
                        runningNames.remove(workName)

                        // Save updated runningNames to preferences
                        preferences.edit().run {
                            putStringSet(prefKeyRunningBackgroundSyncTimes, runningNames)
                            apply()
                        }
                    }
                return true
            }
    }

    suspend fun checkAllBackgroundSyncWork() {
        if (enableBackgroundSync) {
            // Get background-sync-timestamps that should be running
            val timestamps = sharedPreferences.getStringSet(
                prefKeyBackgroundSyncTimes,
                setOf()
            )!!

            // Get background-sync-timestamps that is running already
            val runningTimestamps = sharedPreferences.getStringSet(
                prefKeyRunningBackgroundSyncTimes,
                setOf()
            )!!

            // Get timestamps the should be running but are not running yet
            val timestampsToBeStarted = timestamps.subtract(runningTimestamps)
                .map { timestampString -> timestampString.toLong() }

            // Get timestamps the should be running but are not running yet
            val timestampsToBeStopped = runningTimestamps.subtract(timestamps)
                .map { timestampString -> timestampString.toLong() }

            // Stop all work of timestamps, that should be stopped
            timestampsToBeStopped.forEach { timestampToBeStopped ->
                stopDailyBackgroundSync(timestampToBeStopped)
            }

            // Start all work of timestamps, that should be started
            timestampsToBeStarted.forEach { timestampToBeStarted ->
                startDailyBackgroundSync(timestampToBeStarted)
            }
        } else {
            cancelAllBackgroundSyncWork()
        }
    }

    suspend fun restartAllBackgroundSyncWork() {
        // Cancel all running work
        cancelAllBackgroundSyncWork()

        // Get background-sync-timestamps that should be running
        val timestamps = sharedPreferences.getStringSet(
            prefKeyBackgroundSyncTimes,
            setOf()
        )!!.map { timestampString -> timestampString.toLong() }

        // Start work for all background-sync-timestamps
        timestamps.forEach { timestamp ->
            startDailyBackgroundSync(timestamp)
        }
    }

    suspend fun cancelAllBackgroundSyncWork(): Boolean {
        WorkManager.getInstance(context).cancelAllWorkByTag(
            PERIODIC_BACKGROUND_SYNC_WORK_TAG
        ).await().also {
            return true
        }
    }

    private fun generateDailyWork(timestamp: Long): PeriodicWorkRequest {
        // Find time when this work should first run
        val time = DateTime(timestamp).withZone(DateTimeZone.UTC)
        val timeToday = DateTime.now().withZone(DateTimeZone.UTC)
            .withTime(time.hourOfDay, time.minuteOfHour, 0, 0)

        // If the target time has already passed today, set the first target time to tomorrow at the same time
        val firstTargetDateTime =
            if (!timeToday.isAfterNow) timeToday.plusDays(1) else timeToday

        // Calculate delay to first target time
        val initialDelay = firstTargetDateTime.millis - DateTime.now().millis

        var allowCellularDownloads: Boolean
        var allowRoaming: Boolean

        with(PreferenceManager.getDefaultSharedPreferences(context)) {
            allowCellularDownloads = getBoolean(
                context.resources.getString(R.string.pref_key_allow_cellular_downloads),
                context.resources.getBoolean(R.bool.default_allow_cellular_downloads)
            )
            allowRoaming = getBoolean(
                context.resources.getString(R.string.pref_key_allow_roaming),
                context.resources.getBoolean(R.bool.default_allow_roaming)
            )
        }

        val constraints = Constraints.Builder()
        if (!allowCellularDownloads) {
            constraints.setRequiredNetworkType(NetworkType.UNMETERED)
        } else if (!allowRoaming) {
            constraints.setRequiredNetworkType(NetworkType.NOT_ROAMING)
        }

        return PeriodicWorkRequestBuilder<BackgroundSyncWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints.build())
            .addTag(PERIODIC_BACKGROUND_SYNC_WORK_TAG)
            .build()
    }

    private fun nameFromTimestamp(timestamp: Long): String {
        return "$timestamp"
    }

    companion object {
        private const val PBS = "pbs"
        private const val PERIODIC_BACKGROUND_SYNC_WORK_TAG = "etsapp.$PBS"
    }
}
