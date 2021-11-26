package com.ets.app.ui.fragment.previousplans

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ets.app.R
import com.ets.app.databinding.FragmentPreviousPlansBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PreviousPlansFragment : Fragment() {
    private lateinit var binding: FragmentPreviousPlansBinding
    private lateinit var viewModel: PreviousPlansViewModel
    private lateinit var adapter: PlanInfoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_previous_plans, container, false)
        viewModel = ViewModelProvider(this)[PreviousPlansViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup recyclerview
        binding.recyclerViewPlanInfos.let { recyclerView ->
            // Create adapter and store it to class-attribute 'adapter'
            adapter = PlanInfoAdapter(viewModel)

            // Set adapter of recyclerView
            recyclerView.adapter = adapter

            // Add divider between recyclerView-items
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    (recyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        // Listen for planNames changes
        viewModel.planNames.observe(viewLifecycleOwner) { planNames ->
            // When planNames-value changes, update planInfoItems
            viewModel.updatePlanInfoItems(planNames)
        }

        // Listen for planInfoItems changes
        viewModel.planInfoItems.observe(viewLifecycleOwner) { planInfoItems ->
            // When planInfoItems-value changes publish them to the UI
            adapter.submitList(planInfoItems)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.requestReload()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.option_refresh).isVisible = true
        menu.findItem(R.id.option_switch_to_latest).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_refresh -> {
                viewModel.downloadLatestPlan()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}