package com.inmobi.devtest.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inmobi.devtest.model.Line
import com.inmobi.devtest.model.Lyrics
import com.inmobi.devtest.model.Word

data class WordLayoutCache(
    val word: Word,
    val layoutResult: TextLayoutResult,
    val startX: Float,
    val textY: Float,
    val width: Float,
    val height: Float,
)

data class LineLayoutCache(
    val startY: Float,
    val height: Float,
    val words: List<WordLayoutCache>,
)

@Composable
fun LyricsCanvas(
    lyrics: Lyrics,
    currentLineIndex: Int,
    currentPosition: Long,
    expandProgress: Float,
    onExpand: () -> Unit,
    onCollapseDelta: (Float) -> Unit,
    onCollapseRelease: (Float) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    lineHeightDp: Dp = 40.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.Gray,
    textStyle: TextStyle = TextStyle(fontSize = 18.sp),
    horizontalPaddingDp: Dp = 32.dp,
) {
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val isFullyExpanded = expandProgress > 0.99f

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val lyricsLayoutCache: List<LineLayoutCache> = remember(
        lyrics, containerSize.width, textStyle, lineHeightDp, density
    ) {
        if (containerSize.width == 0) return@remember emptyList()
        val canvasWidth = containerSize.width.toFloat()
        val paddingHorizontal = with(density) { horizontalPaddingDp.toPx() }
        val lineHeightPx = with(density) { lineHeightDp.toPx() }

        buildLyricsLayoutCache(
            lyrics = lyrics,
            textMeasurer = textMeasurer,
            textStyle = textStyle,
            canvasWidth = canvasWidth,
            paddingHorizontal = paddingHorizontal,
            lineHeightPx = lineHeightPx
        )
    }

    LaunchedEffect(currentLineIndex, isFullyExpanded, lyricsLayoutCache) {
        if (currentLineIndex >= 0 && currentLineIndex < lyricsLayoutCache.size && (isFullyExpanded || expandProgress < 0.01f)) {
            scrollState.animateScrollTo(lyricsLayoutCache[currentLineIndex].startY.toInt())
        }
    }

    val totalContentHeight = with(density) {
        (lyricsLayoutCache.lastOrNull()?.let { it.startY + it.height } ?: 0f).toDp()
    }

    val expandProgressRef by rememberUpdatedState(expandProgress)
    val onCollapseDeltaRef by rememberUpdatedState(onCollapseDelta)
    val onCollapseReleaseRef by rememberUpdatedState(onCollapseRelease)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val progress = expandProgressRef
                if (progress <= 0f) return Offset.Zero

                if (progress < 0.99f) {
                    onCollapseDeltaRef(available.y)
                    return Offset(0f, available.y)
                }

                if (scrollState.value == 0 && available.y > 0f) {
                    onCollapseDeltaRef(available.y)
                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val progress = expandProgressRef
                if (progress > 0.01f && progress < 0.99f) {
                    onCollapseReleaseRef(available.y)
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .onSizeChanged { containerSize = it }
            .nestedScroll(nestedScrollConnection)
    ) {
        val currentOnSeek by rememberUpdatedState(onSeek)
        val currentOnExpand by rememberUpdatedState(onExpand)
        val currentExpandProgress by rememberUpdatedState(expandProgress)

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState, enabled = true)
                .height(totalContentHeight)
                .pointerInput(lyricsLayoutCache) {
                    detectTapGestures { offset ->
                        if (currentExpandProgress < 0.5f) {
                            currentOnExpand()
                            return@detectTapGestures
                        }

                        val y = offset.y
                        val lineCache =
                            lyricsLayoutCache.find { y >= it.startY && y < it.startY + it.height }
                        if (lineCache != null) {
                            val x = offset.x
                            for (wc in lineCache.words) {
                                val touchYPadding = 20f
                                if (x in wc.startX..(wc.startX + wc.width) &&
                                    y >= (wc.textY - touchYPadding) && y <= (wc.textY + wc.height + touchYPadding)
                                ) {
                                    val p = (x - wc.startX) / wc.width
                                    val targetTime =
                                        wc.word.startTime + (p * wc.word.duration).toLong()
                                    currentOnSeek(targetTime)
                                    break
                                }
                            }
                        }
                    }
                }
                .drawWithCache {
                    onDrawBehind {
                        val position = currentPosition
                        val visibleStart = scrollState.value.toFloat()
                        val visibleEnd = visibleStart + size.height

                        lyricsLayoutCache.forEach { lc ->
                            if (lc.startY + lc.height >= visibleStart && lc.startY <= visibleEnd) {
                                lc.words.forEach { wc ->
                                    drawText(
                                        wc.layoutResult,
                                        topLeft = Offset(wc.startX, wc.textY),
                                        color = inactiveColor
                                    )

                                    if (position > wc.word.startTime) {
                                        val ratio = if (position >= wc.word.endTime) 1f
                                        else (position - wc.word.startTime).toFloat() / wc.word.duration.toFloat()
                                        if (ratio > 0f) {
                                            clipRect(
                                                left = wc.startX,
                                                top = wc.textY,
                                                right = wc.startX + wc.width * ratio,
                                                bottom = wc.textY + wc.height
                                            ) {
                                                drawText(
                                                    wc.layoutResult,
                                                    topLeft = Offset(wc.startX, wc.textY),
                                                    color = activeColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        )
    }
}

private fun buildLyricsLayoutCache(
    lyrics: Lyrics,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    canvasWidth: Float,
    paddingHorizontal: Float,
    lineHeightPx: Float,
): List<LineLayoutCache> {
    val maxLineWidth = canvasWidth - paddingHorizontal * 2f
    var currentY = 0f

    return lyrics.lines.map { line ->
        val startY = currentY

        val measuredWords = line.words.map { word ->
            val result = textMeasurer.measure(word.text, textStyle, maxLines = 1)
            val w = result.size.width.toFloat()
            val h = result.size.height.toFloat()
            Triple(word, result, Pair(w, h))
        }

        val wordCaches = mutableListOf<WordLayoutCache>()
        var currentLineWidth = 0f
        var physicalLineHeight = lineHeightPx
        val currentPhysicalLineWords =
            mutableListOf<Triple<Word, TextLayoutResult, Pair<Float, Float>>>()
        var textLayoutY = startY

        fun flushPhysicalLine() {
            if (currentPhysicalLineWords.isEmpty()) return
            val cxStart = (canvasWidth - currentLineWidth) / 2f
            var cx = cxStart
            for ((w, result, dimen) in currentPhysicalLineWords) {
                val (width, height) = dimen
                val textY = textLayoutY + (physicalLineHeight - height) / 2f
                wordCaches.add(WordLayoutCache(w, result, cx, textY, width, height))
                cx += width
            }
            textLayoutY += physicalLineHeight
            currentLineWidth = 0f
            currentPhysicalLineWords.clear()
        }

        for (measured in measuredWords) {
            val (_, _, dimen) = measured
            val (w, _) = dimen

            if (currentPhysicalLineWords.isNotEmpty() && currentLineWidth + w > maxLineWidth) {
                flushPhysicalLine()
            }

            currentPhysicalLineWords.add(measured)
            currentLineWidth += w
        }
        flushPhysicalLine()

        val lineTotalHeight = maxOf(textLayoutY - startY, physicalLineHeight)
        currentY += lineTotalHeight

        LineLayoutCache(startY, lineTotalHeight, wordCaches)
    }
}

fun getDummyLyrics(): Lyrics {
    return Lyrics(
        lines = listOf(
            Line(
                listOf(
                    Word("Hồn ", 0, 500),
                    Word("lỡ ", 500, 1000),
                    Word("sa ", 1000, 1500),
                    Word("vào ", 1500, 2000),
                    Word("đôi ", 2000, 2500),
                    Word("mắt ", 2500, 3000),
                    Word("em ", 3000, 3500)
                )
            ),
            Line(
                listOf(
                    Word("Chiều ", 3500, 4000),
                    Word("nao ", 4000, 4500),
                    Word("xõa ", 4500, 5000),
                    Word("tóc ", 5000, 5500),
                    Word("ngồi ", 5500, 6000),
                    Word("bên ", 6000, 7000),
                    Word("rèm ", 7000, 7500),
                )
            ),
        )
    )
}
