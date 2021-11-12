package com.giftmusic.mugip.models

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class OtherUserOnMap(
    val userID : Int,
    val postID : Int,
    val title: String,
    val artist : String,
    val thumbnail : String,
    val tag: String,
    val location : LatLng,
)

data class PostInformation(
    val userID : Int,
    val postID : Int,
    val title: String,
    val artist : String,
    val thumbnail : String,
    val tag: String,
) : Serializable