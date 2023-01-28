package com.example.viva_la_resistance_radio

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Info.newInstance] factory method to
 * create an instance of this fragment.
 */
class Info : Fragment() {
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
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val schedule: TextView = view.findViewById(R.id.schedule)
        val sendmsgBtn: Button = view.findViewById(R.id.send_message)

        Executors.newSingleThreadExecutor().execute {

            val json =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getRadioInfo").readText(
                    Charset.forName("UTF-8")
                ).drop(93)

            schedule.post { schedule.text = json }
        }

        sendmsgBtn.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.send_message)

        val name = EditText(activity)
        name.setHint(R.string.name_hint)
        name.inputType = InputType.TYPE_CLASS_TEXT

        val message = EditText(activity)
        message.setHint(R.string.send_hint)
        message.inputType = InputType.TYPE_CLASS_TEXT

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(name)
        layout.addView(message)
        layout.setPadding(50, 50, 50, 50)

        builder.setView(layout)

        builder.setPositiveButton(R.string.send) { _, _ ->
            val messageSent = (name.text.toString()).plus("\n").plus(message.text.toString())

            sendData(messageSent)


            Toast.makeText(activity, R.string.sent, Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun sendData(messageSent: String) {
        val url = URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=sendChannelMessage&platform=android&TEXT")
        val postData = messageSent

        val connect = url.openConnection()
        connect.doOutput = true


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Info.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Info().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}