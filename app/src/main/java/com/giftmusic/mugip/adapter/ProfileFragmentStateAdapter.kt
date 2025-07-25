package com.giftmusic.mugip.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.giftmusic.mugip.fragment.HistoryFragment
import com.giftmusic.mugip.fragment.PlaylistFragment
import com.giftmusic.mugip.models.MusicItem

class ProfileFragmentStateAdapter(
        fragmentActivity: FragmentActivity,
        private val historyList : ArrayList<MusicItem>,
        private val followList : ArrayList<MusicItem>
    ) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HistoryFragment(historyList)
            else -> PlaylistFragment(followList)
        }
    }
}