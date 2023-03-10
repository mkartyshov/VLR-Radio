package com.mkartyshov.viva_la_resistance_radio

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.httpGet
import com.mkartyshov.viva_la_resistance_radio.R
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Executors


class Info : Fragment() {

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
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        Executors.newSingleThreadExecutor().execute {
            val json =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getRadioInfo").readText(
                    Charset.forName("UTF-8")
                ).drop(93)
            schedule.post { schedule.text = json }
        }

        Handler().postDelayed({schedule.startAnimation(fadeIn)
            schedule.visibility = View.VISIBLE}, 250)

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
            val name = name.text.toString()
            val message = message.text.toString()

            sendData(name, message)

        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendData(name: String, message: String) {
        val url = "https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=sendChannelMessage&"
        val params = mapOf("platform" to "android", "name" to name, "message" to message)
        val sendParams = params.map { (k, v) -> "${k}=${v}" }.joinToString("&")

        (url.plus("").plus(sendParams)).httpGet().response { _, _, _ ->
        }
    }
}