package com.example.viva_la_resistance_radio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.scheduleAtFixedRate

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    //private lateinit var mMusic: Music
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn: Button = view.findViewById(R.id.stop_play)
        val song: TextView = view.findViewById(R.id.song_name)
        val mp = MediaPlayer()
        val url = "https://vivalaresistance.ru/streamradio"

        fun getSongName() {
            val song: TextView = view.findViewById(R.id.song_name)

            Timer().scheduleAtFixedRate(0, 1000) {
                Executors.newSingleThreadExecutor().execute {
                    val title =
                        URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=currentlyPlayingSong").readText(
                            Charset.forName("windows-1251"))
                    if (title.isEmpty()) {
                        song.text = getString(R.string.tech_dif)
                    } else if (mp.isPlaying) song.post { song.text = title }
                }
            }
        }

        btn.setOnClickListener {
            if (mp.isPlaying) {
                mp.stop()
                song.text = getString(R.string.welcome)
            } else {
                mp.seekTo(0)
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
                }
            }


        }


    }

    /*class Music() {
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
    }*/


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Player.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Player().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}