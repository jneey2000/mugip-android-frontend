package com.giftmusic.mugip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.HistoryListAdapter
import com.giftmusic.mugip.models.MusicItem


class HistoryFragment(private val musicList : ArrayList<MusicItem>) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_my_playing_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.music_history)
        val historyListAdapter = HistoryListAdapter(musicList)

        listView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listView.adapter = historyListAdapter
    }
}