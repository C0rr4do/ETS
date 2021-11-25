package com.github.c0rr4do.timelistpreference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.PreferenceManager


class TimeListPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        val persistedValue = getPersistedStringSet(EMPTY_SET)
        if (persistedValue == EMPTY_SET) {
            persistStringSet(setOf())
            updateSummary()
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        updateSummary()
    }

    fun updateSummary() {
        var timestampList = getPersistedStringSet(setOf()).toList().map { time -> time.toLong() }
        timestampList = timestampList.sorted()
        val newSummary = if (timestampList.isNullOrEmpty()) {
            context.resources.getString(R.string.no_times_selected)
        } else {
            timestampList.joinToString { timestamp -> TimeFormatter.formatTime(timestamp) }
        }
        summary = newSummary
    }

    companion object {
        private val EMPTY_SET = setOf("TimeListPreference_null")
    }
}