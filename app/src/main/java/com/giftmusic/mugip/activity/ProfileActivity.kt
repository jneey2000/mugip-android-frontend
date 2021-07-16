package com.giftmusic.mugip.activity

import android.os.Bundle
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
    }
}