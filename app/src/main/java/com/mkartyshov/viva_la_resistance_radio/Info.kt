package com.mkartyshov.viva_la_resistance_radio

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.httpGet
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
        val author: TextView = view.findViewById(R.id.designed)

        Executors.newSingleThreadExecutor().execute {
            val json =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=getRadioInfo").readText(
                    Charset.forName("UTF-8")
                ).drop(93)
            schedule.post { schedule.text = json }
        }

        Handler().postDelayed({schedule.startAnimation(fadeIn)
            schedule.visibility = View.VISIBLE}, 2000)

        sendmsgBtn.setOnClickListener {
            showDialog()
        }

        author.setOnLongClickListener {
            openTelegram()
            true
        }

        author.setOnClickListener {
            Toast.makeText(context, "Long tap to open Telegram", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTelegram() {
        val id = "mkartyshov"
        try {
            val tgm = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$id"))
            startActivity(tgm)
        } catch (e: Exception) {
            val tgmbrowser = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.t.me/$id"))
            startActivity(tgmbrowser)
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

            if (message != "") {
                sendData(name, message)
            } else {
                Toast.makeText(
                    activity,
                    R.string.fill_the_form,
                    Toast.LENGTH_SHORT
                ).show()
                vibrate()
            }
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

        Toast.makeText(
            activity,
            getString(R.string.msg_sent),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= 31) {
            val vibratorManager =
                context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            val v = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 29) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                v.vibrate(100L)
            }
        }
    }
}