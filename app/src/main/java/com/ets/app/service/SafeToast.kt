package com.ets.app.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.ets.app.ETSApplication

object SafeToast {
    fun toastSafely(context: Context, message: String) {
        if (appIsStarted(context)) {
            with(Handler(Looper.getMainLooper())) {
                post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun appIsStarted(context: Context): Boolean {
        return (context.applicationContext as ETSApplication).appIsStarted
    }
}