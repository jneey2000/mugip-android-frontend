package com.giftmusic.mugip.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.response.SearchMusicItem
import com.giftmusic.mugip.models.response.SearchUserItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class SearchSongListViewAdapter(private val mData : ArrayList<SearchMusicItem>) : RecyclerView.Adapter<SearchSongListViewAdapter.ItemViewHolder>(), CoroutineScope {
    private lateinit var job : Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val musicThumbnail : ImageView = itemView!!.findViewById(R.id.song_thumbnail) as ImageView
        private val musicTitle : TextView = itemView!!.findViewById(R.id.song_title) as TextView
        private val musicArtist : TextView = itemView!!.findViewById(R.id.song_artist) as TextView


        fun bind(item: SearchMusicItem){
            musicTitle.text = item.title
            musicArtist.text = item.artist
            if(item.thumbnail != null){
                musicThumbnail.setImageBitmap(item.thumbnail)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_search_result, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.count()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newData : ArrayList<SearchMusicItem>){
        mData.clear()
        mData.addAll(newData)
        notifyDataSetChanged()
    }
}