package com.ets.app.utils

import android.graphics.Paint
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.ets.app.R
import com.ets.app.model.Course
import com.ets.app.model.Subject
import com.ets.app.model.SubstitutionType


@BindingAdapter("courses")
fun TextView.setCourses(courses: Array<Course>) {
    text = courses.joinToString()
}

@BindingAdapter("lessons")
fun TextView.setLessons(lessons: Array<Int>) {
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

@BindingAdapter("subject")
fun TextView.setSubject(subject: Subject) {
    text = context.getString(subject.resourceID)
}

@BindingAdapter("type")
fun TextView.setType(type: SubstitutionType) {
    text = type.description
}

@BindingAdapter("correct")
fun TextView.setCorrect(value: Correctness) {
    when (value) {
        Correctness.CORRECT -> {
            setTextColor(ContextCompat.getColor(context, R.color.correct))
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        Correctness.INCORRECT -> {
            setTextColor(ContextCompat.getColor(context, R.color.incorrect))
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        Correctness.NONE -> {
            // Retrieve default textColor of theme
            val themeTextColor = TypedValue()
            context.theme.resolveAttribute(android.R.attr.textColor, themeTextColor, true)
            setTextColor(themeTextColor.data)
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}