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
import com.giftmusic.mugip.R
import com.giftmusic.mugip.activity.AlarmActivity

class OtherUserFragment : Fragment() {
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

        val showHistoryButton = layout.findViewById<Button>(R.id.show_history_button)
        val showPlaylistButton = layout.findViewById<Button>(R.id.show_playlist_button)
        val primaryColor = resources.getColor(R.color.primary)
        val whiteColor = Color.parseColor("#FFFFFF")

        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val historyFragment = HistoryFragment()
        val playlistFragment = PlaylistFragment()

        showHistoryButton.isSelected = true
        showHistoryButton.setTextColor(primaryColor)
        showPlaylistButton.setTextColor(whiteColor)
        fragmentTransaction.add(R.id.profile_fragment, historyFragment).commit()

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

        val followingButton = layout.findViewById<Button>(R.id.following_button)
        followingButton.setOnClickListener{
            if(followingButton.text == "팔로우") {
                followingButton.text = "팔로잉"
            } else {
                followingButton.text = "팔로우"
            }
        }

        return layout
    }
}