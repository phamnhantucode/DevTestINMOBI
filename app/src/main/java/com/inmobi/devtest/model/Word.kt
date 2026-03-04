package com.inmobi.devtest.model

data class Word(
    val text: String,
    val startTime: Long,
    val endTime: Long
) {
    val duration: Long
        get() = endTime - startTime
}
