package com.ets.app.ui.fragment.previousplans

import androidx.lifecycle.ViewModel

class PlanInfoItem(
    val planName: String,
    val originalFilePath: String?,
    val planDate: String?,
    val downloadDate: String,
    val downloadTime: String
) : ViewModel() {

    private var _expanded = false
    var expanded: Boolean
        get() = _expanded
        set(value) {
            _expanded = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlanInfoItem

        if (planName != other.planName) return false
        if (originalFilePath != other.originalFilePath) return false
        if (planDate != other.planDate) return false
        if (downloadDate != other.downloadDate) return false
        if (downloadTime != other.downloadTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = planName.hashCode()
        result = 31 * result + originalFilePath.hashCode()
        result = 31 * result + planDate.hashCode()
        result = 31 * result + downloadDate.hashCode()
        result = 31 * result + downloadTime.hashCode()
        return result
    }

}
