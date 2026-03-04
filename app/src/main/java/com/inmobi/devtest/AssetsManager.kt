package com.inmobi.devtest

import android.content.Context
import android.content.res.AssetFileDescriptor
import java.io.InputStream

class AssetsManager(private val context: Context) {

    fun getBeatFileDescriptor(): AssetFileDescriptor {
        return context.assets.openFd("beat.mp3")
    }

    fun getBeatInputStream(): InputStream {
        return context.assets.open("beat.mp3")
    }

    fun getLyricsInputStream(): InputStream {
        return context.assets.open("lyrics.xml")
    }

    fun getLyricsString(): String {
        return context.assets.open("lyrics.xml").bufferedReader().use { it.readText() }
    }
}
