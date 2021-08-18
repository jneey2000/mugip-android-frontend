package com.giftmusic.mugip.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.DiggingNotificationItem

class DiggingNotificationListAdapter(list: List<DiggingNotificationItem>) :
    RecyclerView.Adapter<DiggingNotificationListAdapter.ItemViewHolder>() {
    private val mList : List<DiggingNotificationItem> = list

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val userProfileImage : ImageView = itemView!!.findViewById(R.id.notification_user_profile_image) as ImageView
        private val notificationTextView : TextView = itemView!!.findViewById(R.id.notification_text) as TextView

        @SuppressLint("SetTextI18n")
        fun bind(item: DiggingNotificationItem){
            val defaultImage = ResourcesCompat.getDrawable(itemView.resources, R.drawable.user_1, null) as BitmapDrawable
            userProfileImage.setImageDrawable(defaultImage)
            notificationTextView.text = "${item.otherUser.userID}님이 \"${item.contents}\"를 디깅하였습니다."
            (notificationTextView.text as Spannable).setSpan(ForegroundColorSpan(Color.parseColor("#6B63FF")), 0, item.otherUser.userID.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

}