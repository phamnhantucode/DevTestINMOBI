package com.inmobi.devtest

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class Mp3Player(context: Context) {

    private val TAG = "Mp3Engine"

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
        Log.d(TAG, "Initialized with media: $uri")
    }

    init {

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> Log.d(TAG, "Ready — duration: ${player.duration}ms")
                    Player.STATE_ENDED -> Log.d(TAG, "Playback ended")
                    Player.STATE_BUFFERING -> Log.d(TAG, "Buffering...")
                    Player.STATE_IDLE -> Log.d(TAG, "Idle")
                }
            }
        })
    }

    fun play() {
        player.play()
        Log.d(TAG, "play()")
    }

    fun pause() {
        player.pause()
        Log.d(TAG, "pause()")
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        Log.d(TAG, "seekTo($positionMs)")
    }

    fun release() {
        player.release()
        Log.d(TAG, "release()")
    }
}
