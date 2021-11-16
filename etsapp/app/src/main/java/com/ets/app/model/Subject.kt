package com.ets.app.model

import com.ets.app.R

enum class Subject(val id: String, val resourceID: Int) {
    CANCELED("", R.string.canceled),
    TUTORIAL("TUT", R.string.tutorial),
    MATHEMATICS("M", R.string.mathematics),
    PHYSICS("PH", R.string.physics),
    CHEMISTRY("CH", R.string.chemistry),
    BIOLOGY("BIO", R.string.biology),
    INFORMATICS("INFO", R.string.informatics),
    GERMAN("D", R.string.german),
    ENGLISH("E", R.string.english),
    FRENCH("F", R.string.french),
    SPANISH("SPA", R.string.spanish),
    LATIN("L", R.string.latin),
    MUSIC("MU", R.string.music),
    ART("KU", R.string.art),
    DRAMA("DSP", R.string.drama),
    POLITICS_ECONOMICS("POWI", R.string.politics_economics),
    HISTORY("G", R.string.history),
    RELIGION_EV("REV", R.string.religion_ev),
    RELIGION_K("RKA", R.string.religion_cath), // TODO Revise RELIGION_K.id
    ETHICS("ETHI", R.string.ethics),
    GEOGRAPHY("EK", R.string.geography),
    SPORTS("SPO", R.string.sports) // TODO Revise SPORTS.id
}