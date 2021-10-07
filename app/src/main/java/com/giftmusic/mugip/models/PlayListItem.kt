package com.giftmusic.mugip.models

data class PlayListItem(
    val title : String,
    val musicList : List<MusicItem>,
    val category: String
)
