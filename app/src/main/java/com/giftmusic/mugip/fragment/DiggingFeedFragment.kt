package com.giftmusic.mugip.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.giftmusic.mugip.R
import com.giftmusic.mugip.activity.AlarmActivity
import com.giftmusic.mugip.adapter.DiggingFeedFragmentStateAdapter
import com.giftmusic.mugip.models.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DiggingFeedFragment(val user: User) : Fragment() {
    lateinit var viewPager2 : ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("주변", "인기", "태그")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val layout = inflater.inflate(R.layout.fragment_digging_feed, container, false)

        val backButton = layout.findViewById<ImageView>(R.id.back_to_map_from_profile)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 알람 메뉴 버튼
        val openNotificationActivityButton = layout.findViewById<ImageView>(R.id.notification_button_profile)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(activity, AlarmActivity::class.java)
            startActivity(intent)
        }

        viewPager2 = layout.findViewById(R.id.digging_feed_tab_pager)
        tabLayout = layout.findViewById(R.id.digging_feed_tab_layout)
        viewPager2.adapter = DiggingFeedFragmentStateAdapter(this.requireActivity())
        TabLayoutMediator(tabLayout, viewPager2){
                tab, position -> tab.text = tabTextList[position]
        }.attach()

        return layout
    }
}