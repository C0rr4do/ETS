package com.ets.app.ui.fragment.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ets.app.R
import com.ets.app.service.BackgroundSyncService
import com.github.c0rr4do.timelistpreference.TimeListPreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SyncRatePreferencesFragment : TimeListPreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var backgroundSyncService: BackgroundSyncService

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sync_rate_preferences, rootKey)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            resources.getString(R.string.sync_rate_preferences)
    }

    override fun onStart() {
        super.onStart()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.option_refresh).isVisible = false
        menu.findItem(R.id.option_switch_to_latest).isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            resources.getString(R.string.pref_key_background_sync_times) -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.Default) {
                        backgroundSyncService.checkAllBackgroundSyncWork()
                    }
                }
            }
            resources.getString(R.string.pref_key_enable_background_sync) -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.Default) {
                        val enableBackgroundSync =
                            preferenceManager.sharedPreferences.getBoolean(key, false)
                        if (enableBackgroundSync) {
                            backgroundSyncService.restartAllBackgroundSyncWork()
                        } else {
                            backgroundSyncService.cancelAllBackgroundSyncWork()
                        }
                    }
                }
            }
        }
    }
}