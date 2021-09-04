package com.ets.app.model

import java.util.*

class SubstitutionsPlan(
    val downloadTime: Date,
    val uploadTime: Date,
    val date: Date,
    val infoText: String,
    val blockedRooms: Array<String>,
    val substitutions: Array<Substitution>
)