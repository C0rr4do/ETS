package com.ets.app.ui.fragment.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ets.app.R

class SyncRatePreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sync_rate_preferences, rootKey)
    }
}