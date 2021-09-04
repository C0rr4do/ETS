package com.ets.app.model

enum class SubstitutionType(val description: String) {
    SUBSTITUTION("Vertretung"),
    DESPITE_ABSENCE("Trotz Absenz"),
    ROOM_CHANGE("Raumänderung"),
    CANCELED("Frei")
}