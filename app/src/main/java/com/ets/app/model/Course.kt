package com.ets.app.model

class Course(val classID: String, val courseID: String?, val friendlyName: String) {
    override fun toString(): String {
        return friendlyName
    }
}