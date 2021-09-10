package com.ets.app.model

enum class Subject(val id: String, val friendlyName: String) {
    NONE("", "Frei"),
    ENGLISH("E", "Englisch"),
    PHYSICS("PH", "Physik"),
    GERMAN("D", "Deutsch"),
    FRENCH("F", "Französisch"),
    HISTORY("G", "Geschichte"),
    POLITICS_ECONOMY("POWI", "PoWi"),
    LATIN("L", "Latein"),
    MATHEMATICS("M", "Mathe"),
    GEOGRAPHY("EK", "Erdkunde"),
}