package com.example.viva_la_resistance_radio

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
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
@Suppress("DEPRECATION")


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
        val list = view.findViewById<ListView>(R.id.list)

        Timer().scheduleAtFixedRate(0, 300000) {
            getSongList()
        }

        swipe.setOnRefreshListener {
            getSongList()
            swipe.isRefreshing = false
        }

            list.setOnItemClickListener { parent, _, position, _ ->
                val copyText = parent.getItemAtPosition(position).toString()

                val clipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", copyText)
                clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(activity, getString(R.string.copied),Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSongList() {
        val task = object : AsyncTask<Void, Void, Array<String>>() {
            override fun doInBackground(vararg params: Void): Array<String> {
                val url = URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getPlaylist")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val songList = BufferedReader(InputStreamReader(connection.inputStream, "windows-1251"))

                connection.disconnect()
                return songList
                    .readText()
                    .replace("и&#774;", "й")
                    .replace("И&#774;", "Й")
                    .split("\n").dropLast(1).toTypedArray()
            }

            override fun onPostExecute(result: Array<String>) {
                val list = view?.findViewById<ListView>(R.id.list)
                val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,result)
                list?.adapter = arrayAdapter
                if(isAdded){
                    resources.getString(R.string.app_name);
                }
            }
        }
        task.execute()
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