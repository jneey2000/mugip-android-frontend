package com.giftmusic.mugip.models

import android.graphics.Bitmap

data class User(
    val userID : String,
    val nickname : String,
    val userName : String,
    val email : String,
    var profileImage : Bitmap?
)