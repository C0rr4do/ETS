package com.ets.app.ui.fragment.substitutionplan

import androidx.lifecycle.*
import com.ets.app.model.SubstitutionPlan
import com.ets.app.service.FileService
import com.ets.app.service.ParsingService
import com.ets.app.service.SyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubstitutionPlanViewModel @Inject constructor(
    private val fileService: FileService,
    private val syncService: SyncService,
    private val parsingService: ParsingService
) : ViewModel() {
    private val _useLatest = MutableLiveData<Boolean>()
    val useLatest: LiveData<Boolean>
        get() = _useLatest

    private val _selectedPlanName = MutableLiveData<String>()

    val latestPlanName = fileService.latestPlanName

    val planName = Transformations.switchMap(useLatest) { useLatest ->
        if (useLatest) {
            viewModelScope.launch {
                fileService.requestPlanNamesReload()
            }
            latestPlanName
        } else {
            _selectedPlanName
        }
    }

    private val _substitutionPlan = MutableLiveData<SubstitutionPlan?>()
    val substitutionPlan: LiveData<SubstitutionPlan?>
        get() = _substitutionPlan

    val substitutionItems = Transformations.map(substitutionPlan) {
        it?.substitutions?.map { substitution ->
            SubstitutionItem.from(substitution)
        }
    }

    val parsing = parsingService.parsing

    fun setUseLatest(value: Boolean) {
        _useLatest.value = value
    }

    fun selectPlanName(value: String) {
        if (_selectedPlanName.value != value) {
            _selectedPlanName.value = value
        }
    }

    fun downloadLatest() {
        syncService.downloadLatestPlan()
    }

    fun parsePlan() {
        viewModelScope.launch {
            planName.value?.let {
                _substitutionPlan.value = parsingService.getParsedPlan(it)
            }
        }
    }
}