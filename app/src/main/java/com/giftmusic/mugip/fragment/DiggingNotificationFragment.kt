package com.giftmusic.mugip.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.DiggingNotificationListAdapter
import com.giftmusic.mugip.models.DiggingNotificationItem
import com.giftmusic.mugip.models.OtherUserInvite

class DiggingNotificationFragment : Fragment() {
    private val diggingNotificationItemList = ArrayList<DiggingNotificationItem>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_digging_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.digging_notification_list)
        val diggingNotificationListAdapter = DiggingNotificationListAdapter(diggingNotificationItemList)

        listView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listView.adapter = diggingNotificationListAdapter
    }
}