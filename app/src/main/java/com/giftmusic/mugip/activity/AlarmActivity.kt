package com.giftmusic.mugip.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.AlarmFragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AlarmActivity : AppCompatActivity() {
    lateinit var viewPager2 : ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("친구 요청", "디깅 알림")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        viewPager2 = findViewById(R.id.notification_tab_pager)
        tabLayout = findViewById(R.id.notification_tab_layout)
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#4A31A9"))
        init()
    }

    private fun init(){
        viewPager2.adapter = AlarmFragmentStateAdapter(this)
        TabLayoutMediator(tabLayout, viewPager2){
                tab, position -> tab.text = tabTextList[position]
        }.attach()
    }
}