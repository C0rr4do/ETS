package com.ets.app.ui.fragments.substitutionplan

import android.annotation.SuppressLint
import android.graphics.Paint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ets.app.R
import com.ets.app.databinding.SubstitutionItemBinding
import com.ets.app.model.Substitution


class SubstitutionAdapter : ListAdapter<Substitution, ViewHolder>(SleepNightDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class ViewHolder private constructor(private val binding: SubstitutionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Substitution) {
        val courseString = item.courses.joinToString()
        // Course
        binding.textViewCourse.text = courseString

        // SubLessons
        binding.textViewSubLessons.text = item.lessonsString()

        // Subject
        binding.textViewSubject.text = item.subject.friendlyName

        // Lessons
        binding.textViewLessons.text = item.lessonsString()

        // RoomID
        binding.textViewRoomID.text = item.roomID

        // SubSubject
        binding.textViewSubSubject.text = item.subSubject.friendlyName
        if (item.subSubject == item.subject) {
            val value = TypedValue()
            binding.root.context.theme.resolveAttribute(android.R.attr.textColor, value, true)
            binding.textViewSubject.setTextColor(value.data)
        } else {
            binding.textViewSubject.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.incorrect
                )
            )
            binding.textViewSubSubject.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.correct
                )
            )
        }

        // SubRoomID
        binding.textViewSubRoomID.text = item.subRoomID
        if (item.subRoomID == item.roomID) {
            val value = TypedValue()
            binding.root.context.theme.resolveAttribute(android.R.attr.textColor, value, true)
            binding.textViewRoomID.setTextColor(value.data)
            binding.textViewRoomID.paintFlags = binding.textViewRoomID.paintFlags
        } else {
            binding.textViewRoomID.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.incorrect
                )
            )
            binding.textViewRoomID.paintFlags = binding.textViewRoomID.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.textViewSubRoomID.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.correct
                )
            )
        }

        // Type
        binding.textViewType.text = item.type.description

        // InfoText
        if (item.infoText.isEmpty()) {
            binding.textViewInfoText.visibility = View.GONE
        } else {
            binding.textViewInfoText.visibility = View.VISIBLE
            binding.textViewInfoText.text = item.infoText
        }

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: SubstitutionItemBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.substitution_item, parent, false)
            return ViewHolder(binding)
        }
    }
}

class SleepNightDiffCallback : DiffUtil.ItemCallback<Substitution>() {
    override fun areItemsTheSame(oldItem: Substitution, newItem: Substitution): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Substitution, newItem: Substitution): Boolean {
        return oldItem == newItem
    }
}