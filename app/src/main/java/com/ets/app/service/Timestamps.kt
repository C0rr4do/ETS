package com.ets.app.service

import org.joda.time.format.DateTimeFormat

object Timestamps {
    private const val DATE_PATTERN = "dd. MMM yyyy"
    private const val TIME_PATTERN = "HH:mm:ss"

    const val UNKNOWN_TIMESTAMP = -1L

    fun formatTime(timestamp: Long): String {
        return if (timestamp == UNKNOWN_TIMESTAMP) {
            TIME_PATTERN.lowercase().replace("[a-z]", "?")
        } else {
            return format(timestamp, TIME_PATTERN)
        }
    }

    fun formatDate(timestamp: Long): String {
        return if (timestamp == UNKNOWN_TIMESTAMP) {
            DATE_PATTERN.lowercase().replace(Regex("[a-z]"), "?")
        } else {
            format(timestamp, DATE_PATTERN)
        }
    }

    private fun format(timestamp: Long, pattern: String): String {
        return DateTimeFormat.forPattern(pattern).print(timestamp)
    }
}