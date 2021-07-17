package com.giftmusic.mugip.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.PlayListItem

class PlaylistListAdapter(list: List<PlayListItem>) :
    RecyclerView.Adapter<PlaylistListAdapter.ItemViewHolder>() {
    private val mList : List<PlayListItem> = list

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val playListTitle : TextView = itemView!!.findViewById(R.id.play_list_name) as TextView
        private val playListDescription : TextView = itemView!!.findViewById(R.id.play_list_description) as TextView
        private val playListCategoryView : TextView = itemView!!.findViewById(R.id.play_list_category) as TextView


        fun bind(item: PlayListItem){
            playListTitle.text = item.title
            playListDescription.text = "${item.musicList[0].musicTitle} 외 ${item.musicList.count() - 1}곡"
            playListCategoryView.text = item.category
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

}