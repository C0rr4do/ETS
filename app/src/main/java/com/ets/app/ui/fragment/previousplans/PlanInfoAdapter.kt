package com.ets.app.ui.fragment.previousplans

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ets.app.BuildConfig
import com.ets.app.R
import com.ets.app.databinding.ItemPlanInfoBinding
import com.github.c0rr4do.expansionlayout.ExpansionLayout
import java.io.File


class PlanInfoAdapter :
    ListAdapter<PlanInfoItem, PlanInfoViewHolder>(PlanInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanInfoViewHolder {
        return PlanInfoViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PlanInfoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class PlanInfoViewHolder private constructor(private val binding: ItemPlanInfoBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(planInfoItem: PlanInfoItem) {
        binding.planInfoItem = planInfoItem
        binding.expansionLayoutPlanInfo.expansionListener = object: ExpansionLayout.ExpansionListener {
            override fun onExpansionToggle(expansionLayout: ExpansionLayout, expanded: Boolean) {
                super.onExpansionToggle(expansionLayout, expanded)
                binding.planInfoItem?.expanded = expanded
            }
        }
        if (planInfoItem.expanded != binding.expansionLayoutPlanInfo.expanded) {
            binding.expansionLayoutPlanInfo.toggle(false)
        }
        binding.executePendingBindings()
    }

    private fun openPlan() {
        val planName = binding.planInfoItem?.planName
        planName?.let {
            val action =
                PreviousPlansFragmentDirections.actionPreviousPlansFragmentToSubstitutionPlanFragment(
                    it
                )
            binding.root.findNavController().navigate(action)
        }
    }

    private fun openOriginalFile() {
        binding.planInfoItem?.originalFilePath?.let {
            with(binding.root) {
                val file = File(it)

                // Get URI and MIME type of file
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )

                // Open file with user selected app
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(uri, "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(context, intent, null)
            }
        }
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): PlanInfoViewHolder {
            // Inflate layout 'item_plan_info'
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ItemPlanInfoBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_plan_info, parent, false)

            // Create view holder
            val viewHolder = PlanInfoViewHolder(binding)

            // Handle open plan button
            binding.imageViewOpen.setOnClickListener { viewHolder.openPlan() }
            binding.imageViewOpenOriginal.setOnClickListener { viewHolder.openOriginalFile() }

            // These statement starts the marquee animation
            binding.textViewOriginalFile.isSelected = true

            return viewHolder
        }
    }

}

class PlanInfoDiffCallback : DiffUtil.ItemCallback<PlanInfoItem>() {
    override fun areItemsTheSame(oldItem: PlanInfoItem, newItem: PlanInfoItem): Boolean {
        return oldItem.planName == newItem.planName
    }

    override fun areContentsTheSame(oldItem: PlanInfoItem, newItem: PlanInfoItem): Boolean {
        return oldItem == newItem
    }
}
