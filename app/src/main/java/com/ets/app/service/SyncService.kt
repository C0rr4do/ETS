package com.ets.app.service

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.telephony.TelephonyManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.ets.app.R
import com.ets.app.service.SafeToast.toastSafely
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileService: FileService
) {
    private val _syncing = MutableLiveData(false)
    val syncing: LiveData<Boolean>
        get() = _syncing

    private val downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val prefKeyRunningRequestId: String
        get() = context.resources.getString(R.string.pref_key_running_request_id)

    val runningRequestId
        get() = PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(prefKeyRunningRequestId, -1L)

    fun downloadLatestPlan() {
        _syncing.postValue(true)
        removePendingRequest()

        // Generate new request
        val request = generateRequest()

        // Enqueue request to download plan
        val requestId = downloadManager.enqueue(request)

        // Save request-ID to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(context).edit().run {
            putLong(prefKeyRunningRequestId, requestId)
            apply()
        }
    }

    private fun generateRequest(): DownloadManager.Request {
        var allowCellularDownloads: Boolean
        var allowRoaming: Boolean

        // Load values from shared preferences
        PreferenceManager.getDefaultSharedPreferences(context).run {
            allowCellularDownloads = getBoolean(
                context.resources.getString(R.string.pref_key_allow_cellular_downloads),
                context.resources.getBoolean(R.bool.default_allow_cellular_downloads)
            )
            allowRoaming =
                if (allowCellularDownloads) {
                    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                        context.resources.getString(R.string.pref_key_allow_roaming),
                        context.resources.getBoolean(R.bool.default_allow_roaming)
                    )
                } else {
                    false
                }
        }

        // App-name from resources
        val appName = context.resources.getString(R.string.app_name)

        if (!allowCellularDownloads
            && getConnectionType(context) == NetworkCapabilities.TRANSPORT_CELLULAR
        ) {
            toastSafely(context, context.getString(R.string.starting_download_when_wifi_is_available))
        } else if (!allowRoaming && isNetworkRoaming()) {
            toastSafely(context, context.getString(R.string.starting_download_when_network_is_not_roaming_anymore))
        }

        return DownloadManager.Request(Uri.parse(SUBSTITUTION_PLAN_URL)).run {
            setAllowedNetworkTypes(
                if (allowCellularDownloads) {
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                } else {
                    DownloadManager.Request.NETWORK_WIFI
                }
            )
            setAllowedOverRoaming(allowRoaming)
            setTitle(appName)
            setDescription("Downloading plan")
            setDestinationInExternalFilesDir(
                context,
                fileService.plansSubPath,
                "${System.currentTimeMillis()}.pdf"
            )
        }
    }

    private fun removePendingRequest() {
        val runningRequestId = runningRequestId

        if (runningRequestId > 0) {
            if (isRequestPending(runningRequestId)) {
                downloadManager.remove(runningRequestId)
            }

            PreferenceManager.getDefaultSharedPreferences(context).run {
                if (contains(prefKeyRunningRequestId)) {
                    edit().run {
                        remove(prefKeyRunningRequestId)
                        apply()
                    }
                }
            }
        }
    }

    private fun isRequestPending(requestId: Long): Boolean {
        val query = DownloadManager.Query()
        query.setFilterById(requestId)
        val cursor = downloadManager.query(query)
        for (position in 0 until cursor.count) {
            cursor.moveToPosition(position)
            val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusColumnIndex)
            if (status == DownloadManager.STATUS_RUNNING ||
                status == DownloadManager.STATUS_PENDING ||
                status == DownloadManager.STATUS_PAUSED
            ) {
                return true
            }
        }
        return false
    }

    private fun getConnectionType(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (cm != null) {
            val networkCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            if (networkCapabilities != null) {
                return when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        NetworkCapabilities.TRANSPORT_VPN
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        NetworkCapabilities.TRANSPORT_WIFI
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    }
                    else -> {
                        -1
                    }
                }
            }
        }
        return -1
    }

    private fun isNetworkRoaming(): Boolean {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephony.isNetworkRoaming
    }

    suspend fun onDownloadFinished() {
        // Remove pending request id
        PreferenceManager.getDefaultSharedPreferences(context).edit().run {
            remove(prefKeyRunningRequestId)
            apply()
        }

        fileService.onDownloadFinished()
        _syncing.postValue(false)
    }

    companion object {
        const val SUBSTITUTION_PLAN_URL =
            "https://edertalschule.de/service/service-2/vertretungsplan.html?download=69:vertretungsplan-schuelerversion"
    }
}