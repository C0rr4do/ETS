package com.ets.app.model

class SubstitutionPlan(
    val downloadTime: Long,
    val date: Long,
    val infoText: String,
    val blockedRooms: Array<String>,
    val substitutions: List<Substitution>
)