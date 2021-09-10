package com.ets.app.model

import java.util.*

class SubstitutionPlan(
    val downloadTime: Date,
    val uploadTime: Date,
    val date: Date,
    val infoText: String,
    val blockedRooms: List<String>,
    val substitutions: List<Substitution>
)