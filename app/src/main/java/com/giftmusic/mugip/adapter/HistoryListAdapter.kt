package com.giftmusic.mugip.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.MusicItem
import kotlinx.coroutines.*
import java.net.URL
import kotlin.coroutines.CoroutineContext

class HistoryListAdapter(list: List<MusicItem>) :
    RecyclerView.Adapter<HistoryListAdapter.ItemViewHolder>(), CoroutineScope {
    private val mList : List<MusicItem> = list

    private lateinit var job : Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val musicThumbnailView : ImageView = itemView!!.findViewById(R.id.music_thumbnail) as ImageView
        private val musicTitleView : TextView = itemView!!.findViewById(R.id.music_name) as TextView
        private val musicArtistView : TextView = itemView!!.findViewById(R.id.music_artist) as TextView
        private val musicCategoryView : TextView = itemView!!.findViewById(R.id.music_category) as TextView
        private val musicUploadDate : TextView = itemView!!.findViewById(R.id.music_upload_date) as TextView
        private val verticalBar : View = itemView!!.findViewById(R.id.other_user_history_connect_bar) as View


        fun bind(item: MusicItem, index: Int){
            musicTitleView.text = item.musicTitle
            musicArtistView.text = item.artist
            musicCategoryView.text = item.category
            musicUploadDate.text = "${item.createdAt.year}-${item.createdAt.monthValue}-${item.createdAt.dayOfMonth}"

            var thumbnailBitmap : Bitmap?
            job = Job()
            launch {
                val connection = URL(item.musicThumbnailURL).openConnection()
                connection.doInput = true
                connection.connect()

                val thumbnailInputStream = connection.getInputStream()
                thumbnailBitmap = BitmapFactory.decodeStream(thumbnailInputStream)

                withContext(Dispatchers.Main){
                    if(thumbnailBitmap != null){
                        musicThumbnailView.setImageBitmap(thumbnailBitmap)
                    }
                }
            }
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