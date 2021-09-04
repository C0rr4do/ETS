package com.ets.app.model

class Substitution(
    val course: Course,
    val lessons: Array<Int>,
    val subject: Subject,
    val roomID: String,
    val subSubject: Subject,
    val subRoomID: String,
    val subType: SubstitutionType,
    val infoText: String
)