package com.example.viva_la_resistance_radio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Player.newInstance] factory method to
 * create an instance of this fragment.
 */
class Player : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val play: ImageButton = view.findViewById(R.id.stop_play)
        val loading: ProgressBar = view.findViewById(R.id.loading)
        val song: TextView = view.findViewById(R.id.song_name)
        val mp = MediaPlayer()
        val url = "https://vivalaresistance.ru/streamradio"
        val timer: ImageButton = view.findViewById(R.id.timer)

        fun getSongName() {
            val song: TextView = view.findViewById(R.id.song_name)

            Timer().scheduleAtFixedRate(0, 1000) {
                Executors.newSingleThreadExecutor().execute {
                    val title =
                        URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=currentlyPlayingSong").readText(
                            Charset.forName("UTF-8")
                        ).replace("и&#774;", "й").replace("И&#774;", "Й")
                    if (title.isEmpty()) {
                        song.text = getString(R.string.tech_dif)
                    } else if (mp.isPlaying) song.post { song.text = title }
                }
            }
        }

        play.setOnClickListener {
            if (mp.isPlaying) {
                mp.stop()
                song.text = getString(R.string.welcome)
                play.setBackgroundResource(R.drawable.play_button)
            } else {
                play.visibility = View.GONE
                loading.visibility = View.VISIBLE
                mp.seekTo(1)
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mp.setDataSource(url)
                mp.prepareAsync()
                song.text = getString(R.string.buffering)
                mp.setOnPreparedListener {
                    mp.start()
                    getSongName()
                    loading.visibility = View.GONE
                    play.visibility = View.VISIBLE
                    play.setBackgroundResource(R.drawable.stop_button)
                }
            }
        }

        timer.setOnClickListener {
            showTimerSetDialog()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun showTimerSetDialog() {
        val times = arrayOf(
            "5 minutes",
            "10 minutes",
            "15 minutes",
            "30 minutes",
            "45 minutes"
        )
        val builder = AlertDialog.Builder(activity)
        var timer: Long = 0

        builder.setTitle(R.string.set_timer)
            .setSingleChoiceItems(times, -1) { dialog, item ->
                timer = (times[item].filter { it.isDigit() }.toLong()) * 60000
            }
            .setPositiveButton(R.string.chill) { dialog, id ->
                Timer().schedule(timerTask {
                    activity?.finish()
                    exitProcess(0)
                }, timer)
                Toast.makeText(
                    activity,
                    getString(R.string.timer_set) + (timer / 60000) + getString(R.string.chill_time),
                    Toast.LENGTH_LONG
                ).show()
                val btn: ImageButton? = view?.findViewById(R.id.timer)
                btn?.setBackgroundResource(R.drawable.timer_active)
                btn?.imageAlpha = 255
            }
            .setNegativeButton(R.string.go_back) { _, _ ->

            }
            .show()
    }
}