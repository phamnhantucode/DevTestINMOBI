package com.inmobi.devtest

import android.content.Context
import android.content.res.AssetFileDescriptor
import java.io.InputStream

class AssetsManager(private val context: Context) {

    fun getBeatUri(): String {
        return "asset:///beat.mp3"
    }

    fun getLyricsInputStream(): InputStream {
        return context.assets.open("lyrics.xml")
    }

    fun getLyricsString(): String {
        return context.assets.open("lyrics.xml").bufferedReader().use { it.readText() }
    }
}
