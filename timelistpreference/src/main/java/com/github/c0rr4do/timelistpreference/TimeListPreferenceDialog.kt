package com.github.c0rr4do.timelistpreference

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.c0rr4do.timelistpreference.databinding.PreferenceDialogTimeListBinding

internal class TimeListPreferenceDialog : PreferenceDialogFragmentCompat(), TimeEditCallback {

    private lateinit var parentPreference: TimeListPreference

    private lateinit var editedTimes: MutableSet<String>

    private lateinit var binding: PreferenceDialogTimeListBinding
    private lateinit var adapter: TimesAdapter

    override fun onCreateDialogView(context: Context?): View {
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.preference_dialog_time_list,
                null,
                false
            )

        parentPreference = preference as TimeListPreference
        editedTimes = PreferenceManager.getDefaultSharedPreferences(parentPreference.context)
            .getStringSet(parentPreference.key, setOf())!!.toMutableSet()

        binding.recyclerViewTimes.let { recyclerView ->
            adapter = TimesAdapter(this)

            recyclerView.adapter = adapter

            // Add divider between recyclerView-items
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    (recyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        binding.buttonAddTime.setOnClickListener {
            startTimePickerDialog(
                0, 0
            ) { _, hourOfDay, minute ->
                addTime(hourOfDay, minute)
            }
        }

        publishEditedTimesToUI()

        return binding.root
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            // When 'OK' is pressed, persist editedTimes
            parentPreference.persistStringSet(editedTimes)
            parentPreference.updateSummary()
        }
    }

    override fun editTime(timestamp: Long) {
        startTimePickerDialog(
            TimeFormatter.getHour(timestamp), TimeFormatter.getMinute(timestamp)
        ) { _, hourOfDay, minute ->
            editedTimes.remove(timestamp.toString())
            addTime(hourOfDay, minute)
        }
    }

    override fun deleteTime(timestamp: Long) {
        editedTimes.remove(timestamp.toString())
        publishEditedTimesToUI()
    }

    private fun startTimePickerDialog(
        initialHour: Int,
        initialMinute: Int,
        callback: TimePickerDialog.OnTimeSetListener
    ) {
        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            context,
            callback,
            initialHour,
            initialMinute,
            DateFormat.is24HourFormat(context)
        )
        timePickerDialog.show()
    }

    private fun addTime(hour: Int, minute: Int) {
        // Add time to editedTimes
        val timestamp = TimeFormatter.toLocalTimestamp(hour, minute)
        editedTimes.add(timestamp.toString())

        publishEditedTimesToUI()
    }

    private fun publishEditedTimesToUI() {
        // Publish changes to UI
        adapter.submitList(
            editedTimes.map { timestampString ->
                TimeItem(timestampString.toLong())
            }.toList().sortedBy { timeItem -> timeItem.timestamp }
        )
    }

    companion object {
        fun newInstance(key: String): TimeListPreferenceDialog {
            val fragment = TimeListPreferenceDialog()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle

            return fragment
        }
    }
}