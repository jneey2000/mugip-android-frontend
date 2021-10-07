package com.giftmusic.mugip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.PlaylistListAdapter
import com.giftmusic.mugip.models.MusicItem


class PlaylistFragment : Fragment() {
    private val musicList = ArrayList<MusicItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        musicList.clear()
        musicList.add(MusicItem("https://github.com", "멸공의 횃불", "육군", "\uD83D\uDCBB 업무"))
        musicList.add(MusicItem("https://github.com", "아미타이거", "육군", "\uD83D\uDCBB 업무"))
        musicList.add(MusicItem("https://github.com", "전우", "육군", "\uD83D\uDCBB 업무"))

        return inflater.inflate(R.layout.fragment_my_playing_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.music_playlist)
        val playListAdapter = PlaylistListAdapter(musicList)

        listView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listView.adapter = playListAdapter
    }
}