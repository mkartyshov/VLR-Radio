package com.mkartyshov.viva_la_resistance_radio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mkartyshov.viva_la_resistance_radio.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

@Suppress("DEPRECATION")
class LastSongs : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_last_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val swipe: SwipeRefreshLayout = view.findViewById(R.id.swipe)
        val songList = GetSongListAsync().execute().get()
        var listOfSongs = songList
        val list = view.findViewById<ListView>(R.id.list)
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            songList.toTypedArray()
        )
        list.adapter = arrayAdapter
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        fun updateList() {
            val updList = GetSongListAsync().execute().get().toMutableList()
            val oldList = listOfSongs.toMutableList()
            val newList = updList.plus(oldList)
                .toSet()
                .toTypedArray()
                .take(11)

            if (newList.toList() != listOfSongs) {
                listOfSongs = newList.toList()
                activity?.runOnUiThread() {
                    val arrayAdapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOfSongs)
                    list.adapter = arrayAdapter
                }
            }
        }

        Timer().scheduleAtFixedRate(600000, 600000) {
            updateList()
        }

        swipe.setOnRefreshListener {
            list.startAnimation(fadeOut)
            Handler().postDelayed({updateList()
                                  list.visibility = View.INVISIBLE}, 250)
            Handler().postDelayed({list.startAnimation(fadeIn)
                                  list.visibility = View.VISIBLE}, 500)
            swipe.isRefreshing = false
        }

        list.setOnItemClickListener { parent, _, position, _ ->
            val copyText = parent.getItemAtPosition(position).toString()
            val clipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", copyText)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(activity, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }
    }

    class GetSongListAsync : AsyncTask<Void, Void, List<String>>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void): List<String> {
            val url =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getPlaylist")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val songList = BufferedReader(InputStreamReader(connection.inputStream, "windows-1251"))

            connection.disconnect()
            return songList
                .readText()
                .replace("и&#774;", "й")
                .replace("И&#774;", "Й")
                .split("\n")
                .dropLast(1)
                .filterNot { it.contains("VLR Radio - Jingle") }
        }

        override fun onPostExecute(result: List<String>) {
            super.onPostExecute(result)
        }
    }
}