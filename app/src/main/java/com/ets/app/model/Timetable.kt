package com.ets.app.model

class Timetable constructor(
    val entries: Array<MutableList<TimetableEntry>> = Array<MutableList<TimetableEntry>>(5) { mutableListOf<TimetableEntry>() }
) {

}