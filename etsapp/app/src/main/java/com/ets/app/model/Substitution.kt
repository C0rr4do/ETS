package com.ets.app.model

class Substitution(
    val id: Int,
    val courses: Array<Course>,
    val lessons: Array<Int>,
    val subject: Subject,
    val roomID: String,
    val subSubject: Subject,
    val subRoomID: String,
    val type: String,
    val infoText: String
)