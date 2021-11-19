package com.ets.app.ui.fragment.substitutionplan

import com.ets.app.model.Course
import com.ets.app.model.Substitution

class SubstitutionItem(
    val courses: String,
    val lessons: String,
    val subjectResourceId: Int,
    val room: String,
    val subSubjectResourceId: Int,
    val subRoom: String,
    val type: String,
    val infoText: String
) {
    private var _expanded = false
    var expanded: Boolean
        get() = _expanded
        set(value) {
            _expanded = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubstitutionItem

        if (courses != other.courses) return false
        if (lessons != other.lessons) return false
        if (subjectResourceId != other.subjectResourceId) return false
        if (room != other.room) return false
        if (subSubjectResourceId != other.subSubjectResourceId) return false
        if (subRoom != other.subRoom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courses.hashCode()
        result = 31 * result + lessons.hashCode()
        result = 31 * result + subjectResourceId
        result = 31 * result + room.hashCode()
        result = 31 * result + subSubjectResourceId
        result = 31 * result + subRoom.hashCode()
        return result
    }

    companion object {
        fun from(substitution: Substitution): SubstitutionItem {
            return SubstitutionItem(
                courses = formatCourses(substitution.courses),
                lessons = formatLessons(substitution.lessons),
                subjectResourceId = substitution.subject.resourceID,
                room = substitution.roomID,
                subSubjectResourceId = substitution.subSubject.resourceID,
                subRoom = substitution.subRoomID,
                type = substitution.type,
                infoText = substitution.infoText
            )
        }

        private fun formatCourses(courses: List<Course>): String {
            return courses.sortedBy { course -> course.toString() }.joinToString()
        }

        private fun formatLessons(lessons: IntRange): String {
            return if (lessons.start == lessons.endInclusive) {
                "${lessons.start}"
            } else {
                "${lessons.start} - ${lessons.endInclusive}"
            }
        }
    }
}