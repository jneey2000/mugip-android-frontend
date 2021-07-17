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
import com.giftmusic.mugip.models.HistoryListItem

class HistoryListAdapter(list: List<HistoryListItem>) :
    RecyclerView.Adapter<HistoryListAdapter.ItemViewHolder>() {
    private val mList : List<HistoryListItem> = list

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val musicThumbnailView : ImageView = itemView!!.findViewById(R.id.music_thumbnail) as ImageView
        private val musicTitleView : TextView = itemView!!.findViewById(R.id.music_name) as TextView
        private val musicArtistView : TextView = itemView!!.findViewById(R.id.music_artist) as TextView
        private val musicCategoryView : TextView = itemView!!.findViewById(R.id.music_category) as TextView


        fun bind(item: HistoryListItem){
            val defaultImage = ResourcesCompat.getDrawable(itemView.resources, R.drawable.albumart_1, null) as BitmapDrawable
            val bitmap = Bitmap.createScaledBitmap(defaultImage.bitmap, 50, 50, false)

            musicThumbnailView.setImageBitmap(bitmap)
            musicTitleView.text = item.musicTitle
            musicArtistView.text = item.artist
            musicCategoryView.text = item.category
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

}