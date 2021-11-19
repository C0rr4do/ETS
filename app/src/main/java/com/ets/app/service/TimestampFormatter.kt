package com.ets.app.service

import org.joda.time.format.DateTimeFormat

object TimestampFormatter {
    private const val DATE_PATTERN = "dd. MMM yyyy"
    private const val TIME_PATTERN = "HH:mm:ss"

    fun formatTime(timestamp: Long): String {
        return format(timestamp, TIME_PATTERN)
    }

    fun formatDate(timestamp: Long): String {
        return format(timestamp, DATE_PATTERN)
    }

    fun formatDateTime(timestamp: Long): String {
        return format(timestamp, "$DATE_PATTERN - $TIME_PATTERN")
    }

    private fun format(timestamp: Long, pattern: String): String {
        return DateTimeFormat.forPattern(pattern).print(timestamp)
    }
}