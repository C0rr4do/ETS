package com.ets.app.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ets.app.model.Course
import com.ets.app.model.Subject
import com.ets.app.model.SubstitutionType

/*
@BindingAdapter("course")
fun TextView.setCourse(course: Course) {
    text = course.friendlyName
}
@BindingAdapter("subject")
fun TextView.setSubject(subject: Subject) {
    text = subject.friendlyName
}
@BindingAdapter("lessons")
fun TextView.setCourse(lessons: List<Int>) {
    text = when {
        lessons.isEmpty() -> {
            ""
        }
        lessons.size == 1 -> {
            lessons.first().toString()
        }
        else -> {
            "${lessons.first()} - ${lessons.last()}"
        }
    }
}
@BindingAdapter("type")
fun TextView.setType(type: SubstitutionType) {
    text = type.description
}
*/