package com.ets.app.ui.fragments.substitutionplan

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
import com.ets.app.utils.Util

class SubstitutionPlanFragment : Fragment() {

    private lateinit var binding: FragmentSubstitutionPlanBinding
    private lateinit var viewModel: SubstitutionPlanViewModel

    private lateinit var adapter: SubstitutionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this).get(SubstitutionPlanViewModel::class.java)

        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_substitution_plan, container, false)

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
        val downloadTime = Util.getDate(2021, 9, 4, 15, 11, 4)
        val uploadTime = Util.getDate(2021, 9, 2, 18, 58)
        val date = Util.getDate(2021,9,3)
        val infoText = "In der 6. Stunde findet die Vorbereitung der Teams der F-M-Woche statt.\n" +
                "Schüler*innen, die somit nach der 5. Stunde Unterrichtsschluss haben, können nach Hause fahren.\n" +
                "Der Nachmittagsunterricht findet planmäßig statt"
        val blockedRooms = listOf("007", "U6")
        val substitutions = listOf(
            Substitution(0, arrayOf(Course("051a", null, "5a")), arrayOf(2), Subject.ENGLISH, "112", Subject.GEOGRAPHY, "112", SubstitutionType.SUBSTITUTION, ""),
            Substitution(1, arrayOf(Course("061a", null, "6a")), arrayOf(1,2), Subject.PHYSICS, "P1", Subject.PHYSICS, "205", SubstitutionType.SUBSTITUTION, ""),
            Substitution(2, arrayOf(Course("061b", null, "6b")), arrayOf(1), Subject.GERMAN, "212", Subject.GERMAN, "212", SubstitutionType.DESPITE_ABSENCE, ""),
            Substitution(3, arrayOf(Course("061b", null, "6b")), arrayOf(2), Subject.GERMAN, "212", Subject.GERMAN, "212", SubstitutionType.SUBSTITUTION, ""),
            Substitution(4, arrayOf(Course("061e", null, "6e")), arrayOf(1), Subject.GERMAN, "106", Subject.GERMAN, "106", SubstitutionType.SUBSTITUTION, ""),
            Substitution(5, arrayOf(Course("071d", null, "7d"),
                                        Course("071c", null, "7c")), arrayOf(2), Subject.FRENCH, "205", Subject.FRENCH, "208", SubstitutionType.ROOM_CHANGE, ""),
            Substitution(6, arrayOf(Course("091c", null, "9c")), arrayOf(1,2), Subject.HISTORY, "007", Subject.HISTORY, "U6", SubstitutionType.ROOM_CHANGE, ""),
            Substitution(7, arrayOf(Course("091c", null, "9c")), arrayOf(3,4), Subject.MATHEMATICS, "007", Subject.MATHEMATICS, "U6", SubstitutionType.ROOM_CHANGE, ""),
            Substitution(8, arrayOf(Course("091c", null, "9c")), arrayOf(5,6), Subject.POLITICS_ECONOMY, "007", Subject.POLITICS_ECONOMY, "202", SubstitutionType.ROOM_CHANGE, ""),
            Substitution(9, arrayOf(Course("101b", null, "10b")), arrayOf(2), Subject.LATIN, "U3", Subject.LATIN, "U3", SubstitutionType.SUBSTITUTION, ""),
            Substitution(10, arrayOf(Course("E1/2", "m07", "E1/2(m07)")), arrayOf(1,2), Subject.MATHEMATICS, "K2", Subject.MATHEMATICS, "K2", SubstitutionType.CLASS_CHANGED, ""),
            Substitution(11, arrayOf(Course("E1/2", "m08", "E1/2(m08)")), arrayOf(1,2), Subject.MATHEMATICS, "U6", Subject.MATHEMATICS, "E7", SubstitutionType.ROOM_CHANGE, ""),
            Substitution(12, arrayOf(Course("E1/2", "l07", "E1/2(l07)")), arrayOf(3,4), Subject.LATIN, "M5", Subject.NONE, "---", SubstitutionType.CANCELED, ""),
            Substitution(13, arrayOf(Course("E1/2", "d07", "E1/2(d07)")), arrayOf(5), Subject.GERMAN, "D4", Subject.NONE, "---", SubstitutionType.CANCELED, ""),
            Substitution(14, arrayOf(Course("Q1/2", "e08", "Q1/2(e08)")), arrayOf(5), Subject.ENGLISH, "U6", Subject.ENGLISH, "D4", SubstitutionType.ROOM_CHANGE, ""),
            Substitution(15, arrayOf(Course("Q1/2", "ph05", "Q1/2(ph05)")), arrayOf(6), Subject.PHYSICS, "P3", Subject.NONE, "---", SubstitutionType.CANCELED, ""),
            Substitution(16, arrayOf(Course("Q3/4", "E01", "Q3/4(E01)")), arrayOf(3,4), Subject.ENGLISH, "112", Subject.NONE, "---", SubstitutionType.CANCELED, "Aufgaben sind in Moodle gestellt"),
            Substitution(17, arrayOf(Course("Q3/4", "E01", "Q3/4(E02)")), arrayOf(3,4), Subject.ENGLISH, "U6", Subject.ENGLISH, "M5", SubstitutionType.ROOM_CHANGE, ""),
        )
        val sp = SubstitutionPlan(downloadTime, uploadTime, date, infoText, blockedRooms,substitutions)
        viewModel.setSubstitutionPlan(sp)
        adapter.submitList(sp.substitutions)
    }
}