package com.mkartyshov.viva_la_resistance_radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class PlayerService : Service() {
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in the foreground with a notification
        val notification = createNotification()
        startForeground(1, notification)

        // Set up the player
        val dataSource = DefaultHttpDataSource.Factory()
        val stream = MediaItem.fromUri("https://vivalaresistance.ru/streamradio")
        val mediaSource = ProgressiveMediaSource.Factory(dataSource).createMediaSource(stream)
        player.setWakeMode(C.WAKE_MODE_NETWORK)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.setHandleAudioBecomingNoisy(true)

        return START_STICKY
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
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

        // Create the notification and return it
        return NotificationCompat.Builder(this, "player_channel")
            .setContentTitle("Now Playing")
            .setContentText("Some Song Name")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PlayerService::class.java)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}