package com.ets.app.ui.fragment.substitutionplan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ets.app.R
import com.ets.app.databinding.ItemSubstitutionBinding

class SubstitutionAdapter :
    ListAdapter<SubstitutionItem, SubstitutionViewHolder>(SubstitutionItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {
        return SubstitutionViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder, position: Int) {
        val substitutionItem = getItem(position)
        holder.bind(substitutionItem)
    }
}

class SubstitutionViewHolder private constructor(private val binding: ItemSubstitutionBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(substitutionItem: SubstitutionItem) {
        binding.substitutionItem = substitutionItem
        binding.expansionLayoutSubstitution.setExpansionListener { _, expanded ->
            binding.substitutionItem?.expanded = expanded
        }
        if (substitutionItem.expanded != binding.expansionLayoutSubstitution.expanded) {
            binding.expansionLayoutSubstitution.toggle(false)
        }
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): SubstitutionViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ItemSubstitutionBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_substitution, parent, false)

            // These statements start the marquee animation
            binding.textViewCourses.isSelected = true
            binding.textViewSubject.isSelected = true
            binding.textViewSubSubject.isSelected = true

            return SubstitutionViewHolder(binding)
        }
    }
}

class SubstitutionItemCallback : DiffUtil.ItemCallback<SubstitutionItem>() {
    override fun areItemsTheSame(oldItem: SubstitutionItem, newItem: SubstitutionItem): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: SubstitutionItem, newItem: SubstitutionItem): Boolean {
        return oldItem == newItem
    }
}
