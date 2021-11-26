package com.github.c0rr4do.timelistpreference

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class TimeListPreferenceFragmentCompat : PreferenceFragmentCompat() {
    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if(preference is TimeListPreference) {
            val timeListPreferenceDialog = TimeListPreferenceDialog.newInstance(preference.key)
            timeListPreferenceDialog.setTargetFragment(this, 0)
            timeListPreferenceDialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        }
        else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "TimePickerDialog"
    }
}