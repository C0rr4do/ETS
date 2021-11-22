package com.ets.app.ui.fragment.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.ets.app.R

class PreferencesFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.preferences)
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
        updateAllowRoaming()
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            resources.getString(R.string.pref_key_theme) -> {
                val defaultTheme = resources.getString(R.string.default_theme)
                val theme = sharedPreferences.getString(key, defaultTheme)!!.toInt()
                AppCompatDelegate.setDefaultNightMode(theme)
            }
            resources.getString(R.string.pref_key_allow_cellular_downloads) -> {
                updateAllowRoaming()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.option_refresh).isVisible = false
        menu.findItem(R.id.option_switch_to_latest).isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    private fun updateAllowRoaming() {
        // Enabled or disable the allow-roaming-preference depending on the allow-cellular-downloads preference
        findPreference<SwitchPreferenceCompat>(resources.getString(R.string.pref_key_allow_roaming))!!.isEnabled =
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                resources.getString(R.string.pref_key_allow_cellular_downloads),
                resources.getBoolean(R.bool.default_allow_cellular_downloads)
            )
    }
}