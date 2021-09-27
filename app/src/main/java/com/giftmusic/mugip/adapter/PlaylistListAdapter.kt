package com.giftmusic.mugip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.MusicItem

class PlaylistListAdapter(item: List<MusicItem>) :
    RecyclerView.Adapter<PlaylistListAdapter.ItemViewHolder>() {
    private val mList : List<MusicItem> = item

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val playListTitle : TextView = itemView!!.findViewById(R.id.play_list_name) as TextView
        private val playListDescription : TextView = itemView!!.findViewById(R.id.play_list_description) as TextView
        private val playListCategoryView : TextView = itemView!!.findViewById(R.id.play_list_category) as TextView


        fun bind(item: MusicItem){
            playListTitle.text = item.musicTitle
            playListDescription.text = item.artist
            playListCategoryView.text = item.category
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_following_playlist, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

}