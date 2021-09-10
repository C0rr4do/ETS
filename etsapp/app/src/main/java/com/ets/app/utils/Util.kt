package com.ets.app.utils

import java.util.*

class Util {
    companion object {
        private const val NO_YEAR = -1
        private lateinit var cal: Calendar

        fun getDate(
            year: Int = NO_YEAR,
            month: Int = 0,
            dayOfMonth: Int = 0,
            hourOfDay: Int = 0,
            minute: Int = 0,
            second: Int = 0,
        ): Date {
            cal = Calendar.getInstance()
            if (year != NO_YEAR) {
                cal[Calendar.YEAR] = year
                cal[Calendar.MONTH] = month
                cal[Calendar.DAY_OF_MONTH] = dayOfMonth
                cal[Calendar.HOUR_OF_DAY] = hourOfDay
                cal[Calendar.MINUTE] = minute
                cal[Calendar.SECOND] = second
            }
            return cal.time
        }
    }
}
