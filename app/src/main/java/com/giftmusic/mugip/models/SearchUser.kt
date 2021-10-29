package com.giftmusic.mugip.models

data class SearchUserItem(
    val userID : Int,
    val userNickname : String,
    val userEmail : String
)

data class SearchUser(
    val users : ArrayList<SearchUserItem>
)