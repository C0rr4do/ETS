package com.ets.app.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


@DelicateCoroutinesApi
@AndroidEntryPoint
class DownloadBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var syncService: SyncService

    override fun onReceive(context: Context, intent: Intent) {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (downloadId >= 0 && downloadId == syncService.runningRequestId) {
            // Substitution plan download is finished
            // do stuff ...
            GlobalScope.launch {
                syncService.onDownloadFinished()
            }
        }
    }
}