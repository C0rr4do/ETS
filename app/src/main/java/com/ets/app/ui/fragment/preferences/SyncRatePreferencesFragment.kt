package com.ets.app.ui.fragment.preferences

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.ets.app.R

class SyncRatePreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sync_rate_preferences, rootKey)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.sync_rate_preferences)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.option_refresh).isVisible = false
        menu.findItem(R.id.option_switch_to_latest).isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }
}