package com.ets.app.ui

import android.graphics.Paint
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.ets.app.R
import com.ets.app.model.Correctness


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