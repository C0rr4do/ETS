package com.ets.app.model

class Substitution(
    val id: Int,
    val courses: List<Course>,
    val lessons: List<Int>,
    val subject: Subject,
    val roomID: String,
    val subSubject: Subject,
    val subRoomID: String,
    val type: String,
    val infoText: String
)