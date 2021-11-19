package com.github.c0rr4do.expansionlayout

enum class NegativeSizeMode {
    /**
     * Keep and apply negative size values as calculated (can cause graphic bugs with some interpolators)
     */
    KEEP,

    /**
     * (Default) Use absolute value of size
     */
    ABS,

    /**
     * If size is negative, ceil to 0
     */
    CEIL_TO_ZERO
}