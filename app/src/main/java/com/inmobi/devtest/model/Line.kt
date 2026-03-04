package com.inmobi.devtest.model

data class Line(
    val words: List<Word>,
) {
    val text: String
        get() = words.joinToString(separator = "") { it.text }

    val startTime: Long
        get() = words.firstOrNull()?.startTime ?: 0L

    val endTime: Long
        get() = words.lastOrNull()?.endTime ?: 0L
}