package com.inmobi.devtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.inmobi.devtest.ui.screen.PlayerScreen
import com.inmobi.devtest.ui.theme.DevTestINMOBITheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isPlaying by viewModel.isPlaying.collectAsState()
            val currentPosition by viewModel.currentPosition.collectAsState()
            val duration by viewModel.duration.collectAsState()
            val lyrics by viewModel.lyrics.collectAsState()
            val currentLineIndex by viewModel.currentLineIndex.collectAsState()

            DevTestINMOBITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        lyrics?.let { loadedLyrics ->
                            PlayerScreen(
                                lyrics = loadedLyrics,
                                currentLineIndex = currentLineIndex,
                                isPlaying = isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
                                onPlayPauseClick = viewModel::togglePlayPause,
                                onSeek = viewModel::seekTo,
                                onPreviousClick = viewModel::skipBackward,
                                onNextClick = viewModel::skipForward
                            )
                        }
                    }
                }
            }
        }
    }
}
