package com.ets.app.ui.fragments.substitutionplan

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ets.app.model.SubstitutionPlan
import com.ets.app.ui.activities.MainActivity
import com.ets.app.utils.SubstitutionPlanParser
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SubstitutionPlanViewModel : ViewModel() {
    fun setSubstitutionPlan(sp: SubstitutionPlan) {
        _substitutionPlan.value = sp
    }

    private val _substitutionPlan = MutableLiveData<SubstitutionPlan>()
    val substitutionPlan: LiveData<SubstitutionPlan>
        get() = _substitutionPlan

    fun loadSubstitutions(activity: MainActivity) {
        viewModelScope.async {
            var downloader = activity.downloader

            downloader.download{
                var sp = SubstitutionPlanParser.parseSubstitutionPlan(it)
                _substitutionPlan.value = sp
            }

        }
    }
}