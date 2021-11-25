package com.github.c0rr4do.timelistpreference

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.c0rr4do.timelistpreference.databinding.ItemTimeBinding


internal class TimesAdapter(private val timeEditCallback: TimeEditCallback) :
    ListAdapter<TimeItem, TimeViewHolder>(TimeItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        return TimeViewHolder.from(parent, timeEditCallback)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val timeItem = getItem(position)
        holder.bind(timeItem)
    }
}

internal class TimeViewHolder private constructor(
    private val binding: ItemTimeBinding,
    private val timeEditCallback: TimeEditCallback
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(timeItem: TimeItem) {
        binding.timeItem = timeItem
        binding.executePendingBindings()
    }

    private fun editTimeDialog() {
        timeEditCallback.editTime(binding.timeItem!!.timestamp)
    }

    private fun requestDeleteTime() {
        AlertDialog.Builder(binding.root.context)
            .setTitle(binding.root.context.resources.getString(R.string.confirm_removing))
            .setMessage(binding.root.context.resources.getString(R.string.do_you_want_to_remove, binding.timeItem!!.timeString))
            .setIcon(R.drawable.delete)
            .setPositiveButton(binding.root.context.resources.getString(R.string.remove)) { dialog, _ ->
                timeEditCallback.deleteTime(binding.timeItem!! .timestamp)
                dialog.dismiss()
            }
            .setNegativeButton(binding.root.context.resources.getString(R.string.remove)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {
        fun from(parent: ViewGroup, timeEditCallback: TimeEditCallback): TimeViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ItemTimeBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_time, parent, false)

            val viewHolder = TimeViewHolder(binding, timeEditCallback)

            binding.textViewTime.setOnClickListener {
                viewHolder.editTimeDialog()
            }

            binding.imageButtonDeleteTime.setOnClickListener {
                viewHolder.requestDeleteTime()
            }

            return viewHolder
        }
    }
}

internal class TimeItemCallback : DiffUtil.ItemCallback<TimeItem>() {
    override fun areItemsTheSame(oldItem: TimeItem, newItem: TimeItem): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: TimeItem, newItem: TimeItem): Boolean {
        return oldItem == newItem
    }
}