package com.github.c0rr4do.timelistpreference

interface TimeEditCallback {
    fun editTime(timestamp: Long)
    fun deleteTime(timestamp: Long)
}