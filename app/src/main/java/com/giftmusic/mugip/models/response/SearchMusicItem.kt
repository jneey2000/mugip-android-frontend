package com.giftmusic.mugip.models.response

import android.graphics.Bitmap


data class SearchMusicItem(
    val title : String,
    val artist : String,
    val thumbnail : Bitmap?
)
