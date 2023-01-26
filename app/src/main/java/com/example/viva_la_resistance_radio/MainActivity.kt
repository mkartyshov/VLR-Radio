package com.example.viva_la_resistance_radio

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pager = findViewById<ViewPager2>(R.id.viewPager2)
        val tl = findViewById<TabLayout>(R.id.tabLayout)
        pager.adapter = TabsPagerAdapter(supportFragmentManager, lifecycle)

        val tabLogos: Array<Int> = arrayOf(
            R.drawable.info,
            R.drawable.player,
            R.drawable.songlist
        )

        pager.setCurrentItem(1, false)

        TabLayoutMediator(tl, pager) { tab, position ->
            tab.setIcon(tabLogos[position])
        }.attach()
    }
}