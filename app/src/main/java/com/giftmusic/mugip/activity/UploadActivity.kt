package com.giftmusic.mugip.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.*
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.R

class UploadActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        val addTagButton = findViewById<TextView>(R.id.add_tag_button)
        val addTagScrollView = findViewById<ScrollView>(R.id.tag_scroll_view)
        val backButton = findViewById<ImageView>(R.id.back_to_profile)
        val alarmButton = findViewById<ImageView>(R.id.notification_button_profile)
        addTagButton.setOnClickListener {
            when(addTagScrollView.visibility){
                View.VISIBLE -> addTagScrollView.visibility = View.GONE
                View.GONE -> addTagScrollView.visibility = View.VISIBLE
                else -> {}
            }
        }
        backButton.setOnClickListener { finish() }
        alarmButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }
        
        val selectCategoryButton = arrayListOf<Button>(
            findViewById(R.id.select_category_exercise),
            findViewById(R.id.select_category_work),
            findViewById(R.id.select_category_reading),
            findViewById(R.id.select_category_drive),
            findViewById(R.id.select_category_trip),
            findViewById(R.id.select_category_programming),
            findViewById(R.id.select_category_shower)
        )
        selectCategoryButton.map {
            button -> button.setOnClickListener {
                addTagButton.text = button.text
                addTagButton.tag = button.id
            }
        }

        val songSearchBar = findViewById<EditText>(R.id.song_search_bar)
    }
}