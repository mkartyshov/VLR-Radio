package com.mkartyshov.viva_la_resistance_radio

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MusicService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in the foreground with a notification
        val notification = createNotification()
        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotification(): Notification {
        // Create a notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "player_channel",
                "Player Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = Intent.ACTION_MAIN
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Create the notification and return it
        return NotificationCompat.Builder(this, "player_channel")
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.best_in_gtn))
            .setSmallIcon(R.drawable.info_logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
    }

    class SongName : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val url =
                URL("https://vivalaresistance.ru/radio/stuff/vlrradiobot.php?type=currentlyPlayingSong")
            val connect = url.openConnection() as HttpURLConnection
            connect.requestMethod = "GET"
            connect.connect()

            val title = BufferedReader(InputStreamReader(connect.inputStream, "UTF-8"))
            connect.disconnect()
            return title.readText().replace("и&#774;", "й").replace("И&#774;", "Й")
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MusicService::class.java)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timer().purge()
        Timer().cancel()
        Player().mp.stop()
        Player().mp.release()
    }
}