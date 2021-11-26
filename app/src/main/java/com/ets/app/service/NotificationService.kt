package com.ets.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.ets.app.R
import com.ets.app.ui.activity.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.resources.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.resources.getString(R.string.notification_channel_description)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notifyNewPlanAvailable(planName: String) {
        showPlanNotification(ID_NOTIFY_NEW_PLAN_AVAILABLE, planName)
    }

    fun notifyBackgroundSyncRunning() {
        // Create notification
        val builder = defaultNotificationBuilder(
            context.resources.getString(R.string.background_sync),
            context.resources.getString(R.string.background_sync_running),
            NotificationCompat.PRIORITY_LOW
        ).setContentIntent(pendingIntentForDestination(R.id.previousPlansFragment, null))

        // Show notification
        with(NotificationManagerCompat.from(context)) {
            notify(ID_BACKGROUND_SYNC_RUNNING, builder.build())
        }
    }

    fun notifyYourPlanIsUpToDate(planName: String) {
        showPlanNotification(ID_YOUR_PLAN_IS_UP_TO_DATE, planName)
    }

    fun hideBackgroundSyncRunning() {
        with(NotificationManagerCompat.from(context)) {
            cancel(ID_BACKGROUND_SYNC_RUNNING)
        }
    }

    private fun showPlanNotification(id: Int, planName: String) {
        val args = Bundle()
        args.putString("substitutionPlanName", planName)

        val priority = if (id == ID_NOTIFY_NEW_PLAN_AVAILABLE) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_MIN
        }

        val text = if (id == ID_NOTIFY_NEW_PLAN_AVAILABLE) {
            context.resources.getString(R.string.new_plan_available)
        } else {
            context.resources.getString(R.string.your_plan_is_up_to_date)
        }

        // Create notification
        val builder = defaultNotificationBuilder(
            context.resources.getString(R.string.background_sync), text, priority
        ).setContentIntent(pendingIntentForDestination(R.id.substitutionPlanFragment, args))

        // Show notification
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }

    private fun defaultNotificationBuilder(
        title: String, text: String, priority: Int
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)
    }

    private fun pendingIntentForDestination(destinationId: Int, args: Bundle?): PendingIntent {
        return NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.navigation_graph)
            .setDestination(destinationId)
            .setArguments(args)
            .createPendingIntent()
    }

    companion object {
        private const val CHANNEL_ID = "etsapp_background_sync"

        private const val ID_NOTIFY_NEW_PLAN_AVAILABLE = 0
        private const val ID_BACKGROUND_SYNC_RUNNING = 1
        private const val ID_YOUR_PLAN_IS_UP_TO_DATE = 2
    }
}