package com.ets.app.ui.fragment.substitutionplan

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ets.app.R
import com.ets.app.databinding.FragmentSubstitutionPlanBinding
import com.ets.app.service.TimestampFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubstitutionPlanFragment : Fragment() {
    private lateinit var binding: FragmentSubstitutionPlanBinding
    private lateinit var viewModel: SubstitutionPlanViewModel
    private lateinit var optionsMenu: Menu
    private lateinit var adapter: SubstitutionAdapter

    private var previousPlanName: String? = null

    private val selectLatestPlanNameObserver = Observer<String?> {
        it?.let { latestPlanName ->
            viewModel.selectPlanName(latestPlanName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_substitution_plan, container, false)
        viewModel = ViewModelProvider(this)[SubstitutionPlanViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup recyclerview
        binding.recyclerViewSubstitutions.let { recyclerView ->
            // Create adapter and store it to class-attribute 'adapter'
            adapter = SubstitutionAdapter()

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

        // Listen for useLatest changes
        viewModel.useLatest.observe(viewLifecycleOwner) { useLatest ->
            // Update options-menu item
            updateOptionsMenu()
        }

        // Listen for planName changes
        viewModel.planName.observe(viewLifecycleOwner) { planName ->
            if (planName != previousPlanName) {
                viewModel.parsePlan()
                previousPlanName = planName
            }
        }

        viewModel.substitutionPlan.observe(viewLifecycleOwner) { substitutionPlan ->
            substitutionPlan?.date?.let { planDateTimestamp ->
                // Update action bar title to planDate of substitution-plan
                (activity as AppCompatActivity).supportActionBar?.title = TimestampFormatter.formatDate(planDateTimestamp)
            }
        }

        viewModel.substitutionItems.observe(viewLifecycleOwner) { substitutions ->
            adapter.submitList(substitutions ?: listOf())
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = SubstitutionPlanFragmentArgs.fromBundle(requireArguments())

        // Plan name
        val planName = args.substitutionPlanName
        if (planName == null) {
            viewModel.setUseLatest(true)
        } else {
            viewModel.selectPlanName(planName)
            viewModel.setUseLatest(false)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.optionsMenu = menu
        updateOptionsMenu()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_refresh -> {
                viewModel.downloadLatest()
                true
            }
            R.id.option_switch_to_latest -> {
                viewModel.setUseLatest(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateOptionsMenu() {
        if (this::optionsMenu.isInitialized) {
            val useLatest = viewModel.useLatest.value == true
            optionsMenu.findItem(R.id.option_switch_to_latest).isVisible = !useLatest
            optionsMenu.findItem(R.id.option_refresh).isVisible = useLatest
        }
    }
}