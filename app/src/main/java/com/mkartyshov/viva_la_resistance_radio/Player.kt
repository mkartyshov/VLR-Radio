package com.mkartyshov.viva_la_resistance_radio

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

class Player : Fragment() {
    private val url: String = MainActivity().stream
    val mp = MediaPlayer()
    val handler = Handler()

    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("1", "Play")
            activity?.runOnUiThread(
                Runnable {
                    if (!mp.isPlaying) {
                        handler.removeCallbacksAndMessages(null)
                        mp.reset()
                        mp.setDataSource(url)
                        mp.prepareAsync()
                        mp.setOnPreparedListener {
                            mp.start()
                        }
                    }
                })
        }
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("2", "Stop")
            if (mp.isPlaying) {
                mp.stop()
                handler.postDelayed({
                    if (!mp.isPlaying) {
                        activity?.stopService(MusicService.newIntent(requireContext()))
                    }
                }, 120000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter()
        filter.addAction("stop_music")
        ContextCompat.registerReceiver(
            requireContext(),
            stopReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        val filter2 = IntentFilter()
        filter2.addAction("start_music")
        ContextCompat.registerReceiver(
            requireContext(),
            playReceiver,
            filter2,
            ContextCompat.RECEIVER_EXPORTED
        )
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
        val timer: ImageButton = view.findViewById(R.id.timer)
        val livePoint: ImageView = view.findViewById(R.id.live1)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_repeat)

        fun checkForLive(title: String) {
            if (title == "VLR - Live!") {
                activity?.runOnUiThread {
                    livePoint.visibility = View.VISIBLE
                    livePoint.startAnimation(anim)
                }
            } else {
                activity?.runOnUiThread {
                    livePoint.visibility = View.GONE
                    livePoint.clearAnimation()
                }
            }
        }

        fun getSongName() {
            Timer().scheduleAtFixedRate(0, 15000) {
                val title = MusicService.SongName().execute().get()

                if (title.isEmpty() || title == "Radio is Off") {
                    song.text = getString(R.string.tech_dif)
                } else if (mp.isPlaying) {
                    song.post { song.text = title }
                    play.setBackgroundResource(R.drawable.stop_button)
                    checkForLive(title)
                } else {
                    play.setBackgroundResource(R.drawable.play_button)
                    song.text = getString(R.string.welcome)
                }
            }
        }

        fun mpStartStopPlaying() {
            if (mp.isPlaying) {
                activity?.stopService(MusicService.newIntent(requireContext()))
                mp.stop()
                song.text = getString(R.string.welcome)
                play.setBackgroundResource(R.drawable.play_button)
                if (livePoint.isVisible) {
                    livePoint.visibility = View.GONE
                    livePoint.clearAnimation()
                }
            } else {
                play.visibility = View.GONE
                loading.visibility = View.VISIBLE
                song.text = getString(R.string.buffering)
                Thread {
                    activity?.startService(MusicService.newIntent(requireContext()))
                    mp.reset()
                    mp.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    mp.setDataSource(url)
                    mp.prepareAsync()
                    mp.setOnPreparedListener {
                        mp.start()
                        loading.visibility = View.GONE
                        play.visibility = View.VISIBLE
                        play.setBackgroundResource(R.drawable.stop_button)
                        getSongName()
                    }
                }.start()
            }
        }

        play.setOnClickListener {
            mpStartStopPlaying()
        }

        timer.setOnClickListener {
            showTimerSetDialog()
        }
    }

    private fun showTimerSetDialog() {
        val times = arrayOf(
            getString(R.string.five_min),
            getString(R.string.ten_min),
            getString(R.string.fifteen_min),
            getString(R.string.thirty_min),
            getString(R.string.fourtyfive_min)
        )
        val builder = AlertDialog.Builder(activity)
        var timer: Long = 0

        builder.setTitle(R.string.set_timer)
            .setSingleChoiceItems(times, -1) { _, item ->
                timer = (times[item].filter { it.isDigit() }.toLong()) * 60000
            }
            .setPositiveButton(R.string.chill) { _, _ ->
                if (timer > 60000) {
                    Timer().schedule(timerTask {
                        activity?.stopService(MusicService.newIntent(requireContext()))
                        activity?.finish()
                        exitProcess(0)
                    }, timer)
                    Toast.makeText(
                        activity,
                        getString(R.string.timer_set) + (timer / 60000) + getString(R.string.chill_time),
                        Toast.LENGTH_LONG,
                    ).show()
                    val btn: ImageButton? = view?.findViewById(R.id.timer)
                    btn?.setBackgroundResource(R.drawable.timer_active)
                } else {
                }
            }
            .setNegativeButton(R.string.go_back) { _, _ ->
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mp.stop()
        mp.release()
        Timer().purge()
        Timer().cancel()
        activity?.finish()
    }
}