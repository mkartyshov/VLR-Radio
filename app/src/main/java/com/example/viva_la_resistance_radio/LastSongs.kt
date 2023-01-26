package com.example.viva_la_resistance_radio

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
 * Use the [LastSongs.newInstance] factory method to
 * create an instance of this fragment.
 */
class LastSongs : Fragment() {
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

        return inflater.inflate(R.layout.fragment_last_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipe: SwipeRefreshLayout = view.findViewById(R.id.swipe)

        Timer().scheduleAtFixedRate(0, 120000) {
            getSongList()
        }

        swipe.setOnRefreshListener {
            getSongList()
            swipe.isRefreshing = false
        }
    }

    private fun getSongList() {
        val songListText: TextView = requireView().findViewById(R.id.lastSongs)
        Executors.newSingleThreadExecutor().execute {
            val json =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getPlaylist").readText(
                    Charset.forName("windows-1251")
                )
                    .replace("и&#774;", "й")
                    .replace("И&#774;", "Й")

            songListText.post { songListText.text = json }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LastSongs.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LastSongs().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}