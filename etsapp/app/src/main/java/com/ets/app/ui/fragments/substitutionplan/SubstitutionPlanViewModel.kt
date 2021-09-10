package com.ets.app.ui.fragments.substitutionplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ets.app.model.SubstitutionPlan

class SubstitutionPlanViewModel : ViewModel() {
    fun setSubstitutionPlan(sp: SubstitutionPlan) {
        _substitutionPlan.value = sp
    }

    private val _substitutionPlan = MutableLiveData<SubstitutionPlan>()
    val substitutionPlan: LiveData<SubstitutionPlan>
        get() = _substitutionPlan

}