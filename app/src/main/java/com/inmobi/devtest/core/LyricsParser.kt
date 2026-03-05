package com.inmobi.devtest.core

import android.util.Xml
import com.inmobi.devtest.model.Line
import com.inmobi.devtest.model.Lyrics
import com.inmobi.devtest.model.Word
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import kotlin.math.roundToLong

private data class TempWord(val text: String, val startTime: Long)

class LyricsParser {

    fun parse(inputStream: InputStream): Lyrics {
        val lines = mutableListOf<Line>()
        var currentWords = mutableListOf<TempWord>()
        var inParam = false

        try {
            inputStream.use { stream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(stream, null)

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "param" -> {
                                    inParam = true
                                    currentWords = mutableListOf()
                                }

                                "i" -> {
                                    if (inParam) {
                                        val va = parser.getAttributeValue(null, "va")
                                        val timeInSeconds = va?.toFloatOrNull() ?: 0f
                                        val startTime = (timeInSeconds * 1000.0).roundToLong()
                                        val text = parser.nextText()
                                        currentWords.add(TempWord(text, startTime))
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (parser.name == "param") {
                                inParam = false
                                val words = mutableListOf<Word>()
                                val size = currentWords.size
                                for (i in 0 until size) {
                                    val current = currentWords[i]
                                    val endTime = if (i < size - 1) {
                                        currentWords[i + 1].startTime
                                    } else {
                                        current.startTime + 1000L
                                    }
                                    words.add(Word(current.text, current.startTime, endTime))
                                }
                                lines.add(Line(words))
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Lyrics(lines)
    }
}
