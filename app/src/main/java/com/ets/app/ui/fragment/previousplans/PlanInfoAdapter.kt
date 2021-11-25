package com.ets.app.ui.fragment.previousplans

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.ets.app.service.SafeToast.toastSafely
import java.io.File


class PlanInfoAdapter(private val previousPlansViewModel: PreviousPlansViewModel) :
    ListAdapter<PlanInfoItem, PlanInfoViewHolder>(PlanInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanInfoViewHolder {
        return PlanInfoViewHolder.from(parent, previousPlansViewModel)
    }

    override fun onBindViewHolder(holder: PlanInfoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class PlanInfoViewHolder private constructor(
    private val binding: ItemPlanInfoBinding,
    private val previousPlansViewModel: PreviousPlansViewModel
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(planInfoItem: PlanInfoItem) {
        binding.planInfoItem = planInfoItem
        binding.expansionLayoutPlanInfo.setExpansionListener { _, expanded ->
            binding.planInfoItem?.expanded = expanded
        }
        if (planInfoItem.expanded != binding.expansionLayoutPlanInfo.expanded) {
            binding.expansionLayoutPlanInfo.toggle(false)
        }
        binding.executePendingBindings()
    }

    private fun openPlan() {
        binding.planInfoItem?.planName?.let { planName ->
            val action =
                PreviousPlansFragmentDirections
                    .actionPreviousPlansFragmentToSubstitutionPlanFragment(planName)
            binding.root.findNavController().navigate(action)
        }
    }

    private fun openOriginalFile() {
        binding.planInfoItem?.originalFilePath?.let { originalFilePath ->
            with(binding.root) {
                val file = File(originalFilePath)

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
                try {
                    startActivity(context, intent, null)
                } catch (_: ActivityNotFoundException) {
                    toastSafely(
                        context,
                        "Could not find default application for opening PDF-files on device"
                    )
                }
            }
        }
    }

    private fun requestDeleteFile() {
        AlertDialog.Builder(binding.root.context)
            .setTitle(binding.root.context.resources.getString(R.string.confirm_removing))
            .setMessage(
                binding.root.context.resources.getString(
                    R.string.do_you_want_to_delete_plan_for,
                    binding.planInfoItem!!.planDate
                )
            )
            .setIcon(R.drawable.delete)
            .setPositiveButton(binding.root.context.resources.getString(R.string.delete)) { dialog, _ ->
                previousPlansViewModel.deletePlanFile(binding.planInfoItem!!.planName)
                dialog.dismiss()
            }
            .setNegativeButton(binding.root.context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {
        fun from(
            parent: ViewGroup, previousPlansViewModel: PreviousPlansViewModel
        ): PlanInfoViewHolder {
            // Inflate layout 'item_plan_info'
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ItemPlanInfoBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_plan_info, parent, false)

            // Create view holder
            val viewHolder = PlanInfoViewHolder(binding, previousPlansViewModel)

            // Handle open plan button
            binding.imageButtonOpen.setOnClickListener { viewHolder.openPlan() }
            binding.imageButtonOpenOriginal.setOnClickListener { viewHolder.openOriginalFile() }
            binding.imageButtonDeletePlanInfo.setOnClickListener { viewHolder.requestDeleteFile() }

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
