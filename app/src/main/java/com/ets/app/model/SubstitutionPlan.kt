package com.ets.app.model

class SubstitutionPlan(
    val date: Long,
    val infoText: String,
    val blockedRooms: Array<String>,
    val substitutions: List<Substitution>
)