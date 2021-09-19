package com.ets.app.utils

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class Downloader(context: Context) {
    private var queue = Volley.newRequestQueue(context)

    companion object {
        private const val url =
            "https://www.edertalschule.de/service/service-2/vertretungsplan.html?download=69:vertretungsplan-schuelerversion"
    }

    fun download(callback: (ByteArray) -> Unit) {
        val request = ByteRequest(
            url,
            Response.Listener(callback),
            Response.ErrorListener {
                Log.i("Downloader: ", "Error: it.localizedMessage")
            }
        )

        queue.add(request)
    }
}