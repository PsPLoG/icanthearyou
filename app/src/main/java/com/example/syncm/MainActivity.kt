package com.example.syncm

import android.annotation.SuppressLint
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.syncm.ui.theme.SyncMTheme
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyncMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MetronomeUI()
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
        SyncMTheme {
            Greeting("Android")
        }
    }


    @SuppressLint("DiscouragedApi")
    @Composable
    fun MetronomeUI() {
        var bpm by remember { mutableStateOf(120f) }
        var note by remember { mutableStateOf("Quarter Note") }
        var isPlaying by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
        val soundId = remember { soundPool.load(context, R.raw.claves, 1) }
        val scheduler = remember { Executors.newScheduledThreadPool(1) }
        var expanded by remember { mutableStateOf(false) }
        val notes =
            listOf("Whole Note", "Half Note", "Quarter Note", "Eighth Note", "Sixteenth Note")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "BPM: ${bpm.toInt()}")
            Slider(
                value = bpm,
                onValueChange = { bpm = it },
                valueRange = 40f..240f,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.clickable { expanded = true }
            ) {
                notes.forEach { noteOption ->
                    DropdownMenuItem(text = {
                        Text(text = noteOption)
                    }, onClick = {
                        note = noteOption
                        expanded = false
                    })
                }
            }

            Text(text = "Selected Note: $note", modifier = Modifier.padding(vertical = 16.dp))

            Button(onClick = {
                isPlaying = !isPlaying
                val task = Runnable {
                    if (isPlaying) {
                        Log.d("Metronome", "run time(hh:mm:ss): ${System.currentTimeMillis() }")
                        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                    }
                }
                if (isPlaying) {
                    val interval = (60000 / bpm).toLong()
                    val initialDelay = interval - (System.currentTimeMillis() % interval) + (60000 - (System.currentTimeMillis() % 60000))

                    Log.d("Metronome", "start time(hh:mm:ss): ${System.currentTimeMillis() }")
                    scheduler.scheduleAtFixedRate(
                        task,
                        initialDelay,
                        interval,
                        TimeUnit.MILLISECONDS
                    )
                } else {
                    scheduler.shutdownNow()
                }
            }) {
                Text(text = if (isPlaying) "Pause" else "Start")
            }
        }
    }

    @Preview
    @Composable
    fun MetronomeUIPreview() {
        MetronomeUI()
    }
}