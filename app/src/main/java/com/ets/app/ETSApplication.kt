package com.ets.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.ets.app.service.NotificationService
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ETSApplication : Application(), DefaultLifecycleObserver, Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationService: NotificationService

    var appIsStarted = false

    override fun onCreate() {
        super<Application>.onCreate()

        // Setup Timber-Logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Setup PDFBox-Usage
        PDFBoxResourceLoader.init(applicationContext)

        // Setup lifecycle event handling
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Setup notifications
        notificationService.createNotificationChannel()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        appIsStarted = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        appIsStarted = false
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}