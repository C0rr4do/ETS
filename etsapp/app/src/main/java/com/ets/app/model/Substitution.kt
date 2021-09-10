package com.ets.app.model

class Substitution(
    val id: Int,
    val courses: Array<Course>,
    val lessons: List<Int>,
    val subject: Subject,
    val roomID: String,
    val subSubject: Subject,
    val subRoomID: String,
    val type: SubstitutionType,
    val infoText: String
) {
    fun lessonsString(): String {
        return when {
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
}