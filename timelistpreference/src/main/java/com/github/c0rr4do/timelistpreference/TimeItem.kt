package com.github.c0rr4do.timelistpreference

internal class TimeItem(val timestamp: Long) {
    val timeString = TimeFormatter.formatTime(timestamp)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeItem

        if (timestamp != other.timestamp) return false
        if (timeString != other.timeString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + timeString.hashCode()
        return result
    }
}