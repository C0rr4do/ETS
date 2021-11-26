package com.ets.app.ui.fragment.previousplans

import androidx.lifecycle.*
import com.ets.app.service.FileService
import com.ets.app.service.ParsingService
import com.ets.app.service.SyncService
import com.ets.app.util.Timestamps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviousPlansViewModel @Inject constructor(
    private val fileService: FileService,
    private val syncService: SyncService,
    private val parsingService: ParsingService
) : ViewModel() {
    val planNames = fileService.planNames

    private val _planInfoItems = MutableLiveData<MutableList<PlanInfoItem>>(mutableListOf())
    val planInfoItems = Transformations.map(_planInfoItems) { planInfoItems ->
        planInfoItems.toList()
    }

    val reloading = MediatorLiveData<Boolean>().apply {
        addSource(fileService.reloading) {
            value = it || syncService.syncing.value == true
        }
        addSource(syncService.syncing) {
            value = it || fileService.reloading.value == true
        }
    }

    fun requestReload() {
        viewModelScope.launch {
            fileService.requestPlanNamesReload()
        }
    }

    fun downloadLatestPlan() {
        syncService.downloadLatestPlan()
    }

    fun updatePlanInfoItems(planNames: List<String>) {
        viewModelScope.launch {
            val newPlanInfoItems = mutableListOf<PlanInfoItem>()
            planNames.forEach { planName ->
                newPlanInfoItems.add(generatePlanInfoItem(planName))
                _planInfoItems.postValue(newPlanInfoItems)
            }
            if (newPlanInfoItems.isEmpty()) {
                _planInfoItems.postValue(newPlanInfoItems)
            }
        }
    }

    fun deletePlanFile(planName: String) {
        viewModelScope.launch {
            fileService.deletePlanFile(planName)
        }
    }

    private suspend fun generatePlanInfoItem(planName: String): PlanInfoItem {
        val originalFilePath =
            fileService.getFileByName(planName)?.absolutePath

        val planDate = Timestamps.formatDate(parsingService.getParsedDate(planName))

        val downloadDate = Timestamps.formatDate(planName.toLong())

        val downloadTime = Timestamps.formatTime(planName.toLong())

        return PlanInfoItem(
            planName,
            originalFilePath,
            planDate,
            downloadDate,
            downloadTime
        )
    }
}