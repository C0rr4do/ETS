package com.github.c0rr4do.timelistpreference

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

internal object TimeFormatter {
    fun toLocalTimestamp(hour: Int, minute: Int): Long {
        return DateTime(
            1970,
            1,
            1,
            hour,
            minute,
            DateTimeZone.getDefault()
        ).withZone(DateTimeZone.UTC).millis
    }

    fun getHour(timestamp: Long): Int {
        return DateTime(timestamp).hourOfDay
    }

    fun getMinute(timestamp: Long): Int {
        return DateTime(timestamp).minuteOfHour
    }

    fun formatTime(timestamp: Long): String {
        return DateTimeFormat.shortTime().withLocale(Locale.getDefault()).print(timestamp)
    }
}