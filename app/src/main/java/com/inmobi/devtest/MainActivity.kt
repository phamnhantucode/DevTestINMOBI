package com.inmobi.devtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.inmobi.devtest.ui.theme.DevTestINMOBITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            val assetsManager = AssetsManager(this)
            val lyrics = LyricsParser().parse(assetsManager.getLyricsInputStream())
            android.util.Log.d("LyricsParserTest", "Successfully parsed ${lyrics.lines.size} lines from XML.")
            if (lyrics.lines.isNotEmpty()) {
                val firstLine = lyrics.lines.first()
                android.util.Log.d("LyricsParserTest", "First line text: ${firstLine.text}")
                for ((index, word) in firstLine.words.withIndex()) {
                    android.util.Log.d("LyricsParserTest", "Word ${index + 1}: [${word.text}] | Start: ${word.startTime} | Duration: ${word.duration}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LyricsParserTest", "Error parsing lyrics", e)
        }

        setContent {
            DevTestINMOBITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DevTestINMOBITheme {
        Greeting("Android")
    }
}