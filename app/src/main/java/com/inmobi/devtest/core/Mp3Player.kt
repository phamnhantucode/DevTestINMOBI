package com.inmobi.devtest.core

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class Mp3Player(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    val isPlaying: Boolean
        get() = player.isPlaying

    val currentPosition: Long
        get() = player.currentPosition

    val duration: Long
        get() = player.duration

    fun loadFile(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun release() {
        player.release()
    }
}
