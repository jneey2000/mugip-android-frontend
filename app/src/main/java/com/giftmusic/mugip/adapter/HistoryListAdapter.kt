package com.giftmusic.mugip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.MusicItem

class HistoryListAdapter(list: List<MusicItem>) :
    RecyclerView.Adapter<HistoryListAdapter.ItemViewHolder>() {
    private val mList : List<MusicItem> = list

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val musicThumbnailView : ImageView = itemView!!.findViewById(R.id.music_thumbnail) as ImageView
        private val musicTitleView : TextView = itemView!!.findViewById(R.id.music_name) as TextView
        private val musicArtistView : TextView = itemView!!.findViewById(R.id.music_artist) as TextView
        private val musicCategoryView : TextView = itemView!!.findViewById(R.id.music_category) as TextView
        private val verticalBar : View = itemView!!.findViewById(R.id.other_user_history_connect_bar) as View


        fun bind(item: MusicItem, index: Int){
//            musicThumbnailView.setBackgroundResource(R.drawable.albumart_1)
            musicTitleView.text = item.musicTitle
            musicArtistView.text = item.artist
            musicCategoryView.text = item.category

            if(index == 0){
                verticalBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playing_histroy, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position], position)
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

}