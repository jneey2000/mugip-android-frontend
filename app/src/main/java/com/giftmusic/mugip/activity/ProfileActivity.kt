package com.giftmusic.mugip.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.giftmusic.mugip.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<ImageView>(R.id.back_to_map_from_profile)
        backButton.setOnClickListener {
            finish()
        }

        val showHistoryButton = findViewById<Button>(R.id.show_history_button)
        val showPlaylistButton = findViewById<Button>(R.id.show_playlist_button)
        val primaryColor = resources.getColor(R.color.primary)
        val whiteColor = Color.parseColor("#FFFFFF")


        when(intent.getIntExtra("initialFragment", 0)){

            0 -> {
                showHistoryButton.isSelected = true
                showHistoryButton.setTextColor(primaryColor)
                showPlaylistButton.setTextColor(whiteColor)
            }

            1 -> {
                showPlaylistButton.isSelected = true
                showHistoryButton.setTextColor(whiteColor)
                showPlaylistButton.setTextColor(primaryColor)
            }
        }

        // 버튼 클릭시
        showHistoryButton.setOnClickListener {
            showHistoryButton.setTextColor(primaryColor)
            showPlaylistButton.setTextColor(whiteColor)
            showHistoryButton.isSelected = true
            showPlaylistButton.isSelected = false
        }

        showPlaylistButton.setOnClickListener {
            showHistoryButton.setTextColor(whiteColor)
            showPlaylistButton.setTextColor(primaryColor)
            showHistoryButton.isSelected = false
            showPlaylistButton.isSelected = true
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