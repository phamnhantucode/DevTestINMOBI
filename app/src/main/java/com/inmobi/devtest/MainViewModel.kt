package com.inmobi.devtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inmobi.devtest.core.AssetsManager
import com.inmobi.devtest.core.LyricsParser
import com.inmobi.devtest.core.Mp3Player
import com.inmobi.devtest.model.Lyrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mp3Player = Mp3Player(application)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics = _lyrics.asStateFlow()

    val currentLineIndex = combine(_lyrics, _currentPosition) { lyrics, position ->
        lyrics?.lines?.indexOfLast { it.startTime <= position } ?: -1
    }.stateIn(viewModelScope, SharingStarted.Eagerly, -1)

    init {
        val assetsManager = AssetsManager(application)
        mp3Player.loadFile(assetsManager.getBeatUri())
        mp3Player.play()

        viewModelScope.launch {
            try {
                _lyrics.value = LyricsParser().parse(assetsManager.getLyricsInputStream())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        viewModelScope.launch {
            while (true) {
                _isPlaying.value = mp3Player.isPlaying
                _currentPosition.value = mp3Player.currentPosition
                _duration.value = mp3Player.duration
                delay(100)
            }
        }
    }

    fun togglePlayPause() {
        if (mp3Player.isPlaying) {
            mp3Player.pause()
        } else {
            mp3Player.play()
        }
    }

    fun seekTo(position: Long) {
        mp3Player.seekTo(position)
    }

    fun skipForward() {
        mp3Player.seekTo((_currentPosition.value + 5000).coerceAtMost(_duration.value))
    }

    fun skipBackward() {
        mp3Player.seekTo((_currentPosition.value - 5000).coerceAtLeast(0))
    }

    override fun onCleared() {
        super.onCleared()
        mp3Player.release()
    }
}
