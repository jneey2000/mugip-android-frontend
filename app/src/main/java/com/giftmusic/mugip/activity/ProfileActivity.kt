package com.giftmusic.mugip.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.giftmusic.mugip.R
import com.giftmusic.mugip.fragment.HistoryFragment
import com.giftmusic.mugip.fragment.PlaylistFragment

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<ImageView>(R.id.back_to_map_from_profile)
        backButton.setOnClickListener {
            finish()
        }

        // 알람 메뉴 버튼
        val openNotificationActivityButton = findViewById<ImageView>(R.id.notification_button_profile)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }

        val showHistoryButton = findViewById<Button>(R.id.show_history_button)
        val showPlaylistButton = findViewById<Button>(R.id.show_playlist_button)
        val primaryColor = resources.getColor(R.color.primary)
        val whiteColor = Color.parseColor("#FFFFFF")

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val historyFragment = HistoryFragment()
        val playlistFragment = PlaylistFragment()

        when(intent.getIntExtra("initialFragment", 0)){

            0 -> {
                showHistoryButton.isSelected = true
                showHistoryButton.setTextColor(primaryColor)
                showPlaylistButton.setTextColor(whiteColor)
                fragmentTransaction.add(R.id.profile_fragment, historyFragment)

            }

            1 -> {
                showPlaylistButton.isSelected = true
                showHistoryButton.setTextColor(whiteColor)
                showPlaylistButton.setTextColor(primaryColor)
                fragmentTransaction.add(R.id.profile_fragment, playlistFragment)
            }
        }
        fragmentTransaction.commit()

        // 버튼 클릭시
        showHistoryButton.setOnClickListener {
            showHistoryButton.setTextColor(primaryColor)
            showPlaylistButton.setTextColor(whiteColor)
            showHistoryButton.isSelected = true
            showPlaylistButton.isSelected = false
            fragmentManager.beginTransaction().replace(R.id.profile_fragment, historyFragment).commit()
        }

        showPlaylistButton.setOnClickListener {
            showHistoryButton.setTextColor(whiteColor)
            showPlaylistButton.setTextColor(primaryColor)
            showHistoryButton.isSelected = false
            showPlaylistButton.isSelected = true
            fragmentManager.beginTransaction().replace(R.id.profile_fragment, playlistFragment).commit()
        }

        val followingButton = findViewById<Button>(R.id.following_button)
        followingButton.setOnClickListener{
            if(followingButton.text == "팔로우") {
                followingButton.text = "팔로잉"
            } else {
                followingButton.text = "팔로우"
            }
        }


    }
}