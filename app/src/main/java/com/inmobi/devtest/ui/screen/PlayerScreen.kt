package com.inmobi.devtest.ui.screen

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inmobi.devtest.R
import com.inmobi.devtest.model.Lyrics
import com.inmobi.devtest.ui.component.LyricsCanvas
import com.inmobi.devtest.ui.component.getDummyLyrics
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    lyrics: Lyrics = getDummyLyrics(),
    currentLineIndex: Int = 0,
    isPlaying: Boolean = false,
    currentPosition: Long = 0L,
    duration: Long = 0L,
    onPlayPauseClick: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var expandProgress by remember { mutableFloatStateOf(0f) }
    val isExpanded = expandProgress > 0.99f

    val collapseDistancePx = with(density) { 300.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val diskWeight = (1f - expandProgress).coerceAtLeast(0.001f)

        Box(
            modifier = Modifier
                .weight(diskWeight)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = (1f - expandProgress * 2f).coerceIn(0f, 1f)
                }
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = "Album Art",
                    modifier = Modifier.size(120.dp),
                    tint = Color.White
                )
            }
        }

        val lyricsWeight = (0.25f + expandProgress * 0.75f).coerceAtLeast(0.001f)

        LyricsCanvas(
            lyrics = lyrics,
            currentLineIndex = currentLineIndex,
            currentPosition = currentPosition,
            expandProgress = expandProgress,
            onExpand = {
                scope.launch {
                    animate(
                        expandProgress,
                        1f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                    ) { value, _ ->
                        expandProgress = value
                    }
                }
            },
            onCollapseDelta = { deltaPx ->
                val delta = deltaPx / collapseDistancePx
                expandProgress = (expandProgress - delta).coerceIn(0f, 1f)
            },
            onCollapseRelease = { velocityY ->
                val target = if (expandProgress < 0.5f || velocityY > 1000f) 0f else 1f
                scope.launch {
                    animate(
                        expandProgress,
                        target,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                    ) { value, _ ->
                        expandProgress = value
                    }
                }
            },
            onSeek = onSeek,
            modifier = Modifier
                .weight(lyricsWeight)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            val sliderValue =
                if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

            Slider(
                value = sliderValue,
                onValueChange = { fraction ->
                    if (duration > 0) {
                        onSeek((fraction * duration).toLong())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Shuffle */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shuffle),
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onPreviousClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_5sback),
                    contentDescription = "Rewind 5s",
                    modifier = Modifier.size(24.dp)
                )
            }

            FloatingActionButton(onClick = onPlayPauseClick, shape = CircleShape) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = onNextClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_5sforward),
                    contentDescription = "Forward 5s",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_repeat),
                    contentDescription = "Repeat",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "00:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    PlayerScreen()
}
