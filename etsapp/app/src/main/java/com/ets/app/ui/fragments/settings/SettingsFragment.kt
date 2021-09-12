package com.ets.app.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ets.app.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listeners for settings changes

        // Theme preference
        findPreference<ListPreference>(getString(R.string.pref_key_theme))?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                AppCompatDelegate.setDefaultNightMode((newValue as String).toInt())
                true

            }

        // Theme preference
        findPreference<ListPreference>(getString(R.string.pref_key_theme))?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                AppCompatDelegate.setDefaultNightMode((newValue as String).toInt())
                true

            }
    }
}