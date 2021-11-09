package com.giftmusic.mugip.models

import java.time.LocalDateTime

data class MusicItem(
    val musicThumbnailURL : String,
    val musicTitle : String,
    val artist : String,
    val category: String,
    val createdAt: LocalDateTime
)
