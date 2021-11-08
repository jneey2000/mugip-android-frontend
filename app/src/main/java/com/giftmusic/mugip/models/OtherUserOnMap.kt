package com.giftmusic.mugip.models

import com.google.android.gms.maps.model.LatLng

data class OtherUserOnMap(
    val userID : Int,
    val title: String,
    val artist : String,
    val thumbnail : String,
    val tag: String,
    val location : LatLng,
)