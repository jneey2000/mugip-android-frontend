package com.giftmusic.mugip.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.giftmusic.mugip.R
import com.giftmusic.mugip.activity.AlarmActivity
import com.giftmusic.mugip.adapter.AlarmFragmentStateAdapter
import com.giftmusic.mugip.adapter.OtherUserFragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OtherUserFragment : Fragment() {
    lateinit var viewPager2 : ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("History", "Others")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val layout = inflater.inflate(R.layout.fragment_other_user, container, false)

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

        val followingButton = layout.findViewById<Button>(R.id.following_button)
        followingButton.setOnClickListener{
            if(followingButton.text == "팔로우") {
                followingButton.text = "팔로잉"
            } else {
                followingButton.text = "팔로우"
            }
        }

        viewPager2 = layout.findViewById(R.id.other_user_tab_pager)
        tabLayout = layout.findViewById(R.id.other_user_tab_layout)
        viewPager2.adapter = OtherUserFragmentStateAdapter(this.requireActivity())
        TabLayoutMediator(tabLayout, viewPager2){
                tab, position -> tab.text = tabTextList[position]
        }.attach()
        return layout
    }
}