package com.giftmusic.mugip.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.response.SearchUserItem

class SearchUserListViewAdapter(private val mData : ArrayList<SearchUserItem>) : RecyclerView.Adapter<SearchUserListViewAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val otherUserProfile : ImageView = itemView!!.findViewById(R.id.user_search_item_profile) as ImageView
        private val otherUserNickname : TextView = itemView!!.findViewById(R.id.user_search_item_nickname) as TextView
        private val otherUserEmail : TextView = itemView!!.findViewById(R.id.user_search_item_email) as TextView


        fun bind(item: SearchUserItem){
            if(item.userID < 0 && item.userEmail.isEmpty()){
                otherUserProfile.visibility = View.GONE
                otherUserEmail.visibility = View.GONE
            }
            otherUserNickname.text = item.userNickname
            otherUserEmail.text = item.userEmail
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_user_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.count()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newData : ArrayList<SearchUserItem>){
        mData.clear()
        mData.addAll(newData)
        notifyDataSetChanged()
    }
}