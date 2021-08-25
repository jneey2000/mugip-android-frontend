package com.giftmusic.mugip.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.OtherUserInvite

class InvitationListAdapter(list: List<OtherUserInvite>) :
    RecyclerView.Adapter<InvitationListAdapter.ItemViewHolder>() {
    private val mList : List<OtherUserInvite> = list

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val userProfileImage : ImageView = itemView!!.findViewById(R.id.invitation_user_profile_image) as ImageView
        private val userProfileID : TextView = itemView!!.findViewById(R.id.invitation_id) as TextView
        private val userProfileNickname : TextView = itemView!!.findViewById(R.id.invitation_user_nickname) as TextView


        fun bind(item: OtherUserInvite){
            val defaultImage = ResourcesCompat.getDrawable(itemView.resources, R.drawable.user_1, null) as BitmapDrawable

            userProfileImage.setImageDrawable(defaultImage)
            userProfileID.text = item.userID
            userProfileNickname.text = item.userNickname
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invitation, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

}