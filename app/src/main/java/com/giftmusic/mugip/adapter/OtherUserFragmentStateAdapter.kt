package com.giftmusic.mugip.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.giftmusic.mugip.fragment.AddFriendsFragment
import com.giftmusic.mugip.fragment.DiggingNotificationFragment
import com.giftmusic.mugip.fragment.HistoryFragment
import com.giftmusic.mugip.fragment.PlaylistFragment

class OtherUserFragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HistoryFragment()
            else -> PlaylistFragment()
        }
    }
}