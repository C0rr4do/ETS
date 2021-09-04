package com.ets.app.utils

import com.ets.app.model.Course

class GeneratorUtils {
    companion object {
        fun generateCourse(classID: String, courseID: String? = null): Course {
            val friendlyName = classID // TODO generate friendly name from class ID
            return Course(classID, courseID, friendlyName)
        }
    }
}