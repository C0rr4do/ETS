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
        binding.substitution = item

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