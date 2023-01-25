package com.example.viva_la_resistance_radio

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : AppCompatActivity() {

    private lateinit var mMusic: Music

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mMusic = Music()

        val btn = findViewById<Button>(R.id.stop_play)
        val song = findViewById<TextView>(R.id.song_name)

        btn.setOnClickListener { // if playing => stop else play
            if (mMusic.mp.isPlaying) {
                mMusic.mp.stop()
                song.text = getString(R.string.welcome)
            } else {
                mMusic.mp.seekTo(0)
                mMusic.prepare()
                song.text = getString(R.string.buffering)
                mMusic.mp.setOnPreparedListener {
                    mMusic.mp.start()
                    getSongName()
                }
            }
        }
    }

    class Music() {
        val mp = MediaPlayer()
        private val url = "https://vivalaresistance.ru/streamradio"

        fun prepare() {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(url)
            mp.prepareAsync() // might take long! (for buffering, etc)
        }
    }

    private fun getSongName() {
        val song = findViewById<TextView>(R.id.song_name)

        Timer().scheduleAtFixedRate(0, 30000) {
            Executors.newSingleThreadExecutor().execute {
                val title =
                    URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=currentlyPlayingSong").readText()
                if (title.isEmpty()) {
                    song.text = getString(R.string.tech_dif)
                } else if (mMusic.mp.isPlaying) song.post { song.text = title }
            }
        }
    }
}