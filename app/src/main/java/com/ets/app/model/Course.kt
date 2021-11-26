package com.ets.app.model

class Course(private val classID: Int, private val courseID: String?) {
    override fun toString(): String {
        return when (classID) {
            11 -> "E1/2 $courseID"
            12 -> "Q1/2 $courseID"
            13 -> "Q3/4 $courseID"
            else -> "$classID$courseID"
        }
    }
}