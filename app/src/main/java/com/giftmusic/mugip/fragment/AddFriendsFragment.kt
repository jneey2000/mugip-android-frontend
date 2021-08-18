package com.giftmusic.mugip.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.HistoryListAdapter
import com.giftmusic.mugip.adapter.InvitationListAdapter
import com.giftmusic.mugip.models.OtherUserInvite

class AddFriendsFragment : Fragment() {
    private val invitationList = ArrayList<OtherUserInvite>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        invitationList.clear()
        invitationList.add(OtherUserInvite("jil8885", "이정인", "https://github.com"))

        return inflater.inflate(R.layout.fragment_add_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<RecyclerView>(R.id.invitation_list)
        val invitationListAdapter = InvitationListAdapter(invitationList)

        listView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listView.adapter = invitationListAdapter
    }
}