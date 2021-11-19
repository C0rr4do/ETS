package com.ets.app.model

import com.ets.app.R

enum class Subject(val id: String, val resourceID: Int) {
    CANCELED("", R.string.subject_canceled),
    TUTORIAL("TUT", R.string.subject_tutorial),
    MATHEMATICS("M", R.string.subject_mathematics),
    PHYSICS("PH", R.string.subject_physics),
    CHEMISTRY("CH", R.string.subject_chemistry),
    BIOLOGY("BIO", R.string.subject_biology),
    INFORMATICS("INFO", R.string.subject_informatics),
    GERMAN("D", R.string.subject_german),
    ENGLISH("E", R.string.subject_english),
    FRENCH("F", R.string.subject_french),
    SPANISH("SPA", R.string.subject_spanish),
    LATIN("L", R.string.subject_latin),
    MUSIC("MU", R.string.subject_music),
    ART("KU", R.string.subject_art),
    DRAMA("DSP", R.string.subject_drama),
    POLITICS_ECONOMICS("POWI", R.string.subject_politics_economics),
    HISTORY("G", R.string.subject_history),
    RELIGION_EV("REV", R.string.subject_religion_ev),
    RELIGION_K("RKA", R.string.subject_religion_cath),
    ETHICS("ETHI", R.string.subject_ethics),
    GEOGRAPHY("EK", R.string.subject_geography),
    SPORTS("SPO", R.string.subject_sports),
    REMEDIATION("STFÖ", R.string.subject_remediation),
    UNKNOWN("UNKNOWN", R.string.subject_unknown)
}