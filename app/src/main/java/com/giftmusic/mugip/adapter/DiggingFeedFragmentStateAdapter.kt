package com.giftmusic.mugip.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.giftmusic.mugip.fragment.HistoryFragment
import com.giftmusic.mugip.fragment.PlaylistFragment

class DiggingFeedFragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HistoryFragment()
            else -> PlaylistFragment()
        }
    }
}