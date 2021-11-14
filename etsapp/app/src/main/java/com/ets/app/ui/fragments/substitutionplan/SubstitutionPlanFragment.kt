package com.ets.app.ui.fragments.substitutionplan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ets.app.R
import com.ets.app.databinding.FragmentSubstitutionPlanBinding
import com.ets.app.model.*
import com.ets.app.ui.activities.MainActivity
import com.ets.app.utils.Downloader
import com.ets.app.utils.SubstitutionPlanParser
import com.ets.app.utils.Util
import kotlinx.coroutines.*

class SubstitutionPlanFragment : Fragment() {

    private lateinit var binding: FragmentSubstitutionPlanBinding
    private lateinit var viewModel: SubstitutionPlanViewModel

    private lateinit var adapter: SubstitutionAdapter
    private lateinit var downloader: Downloader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this).get(SubstitutionPlanViewModel::class.java)

        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_substitution_plan, container, false)

        downloader = (activity as MainActivity).downloader

        adapter = SubstitutionAdapter()
        binding.recyclerViewSubstitutions.adapter = adapter

        viewModel.substitutionPlan.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it.substitutions)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runBlocking {
            launch {
                downloader.download {
                    val sp = SubstitutionPlanParser.parseSubstitutionPlan(it)
                    viewModel.setSubstitutionPlan(sp)
                    adapter.submitList(sp.substitutions)
                }
            }
        }
    }
}