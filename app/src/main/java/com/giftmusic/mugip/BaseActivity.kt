package com.giftmusic.mugip

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    fun progressOn(){
        BaseApplication.getInstance().progressON(this, null)
    }

    fun progressOn(message: String){
        BaseApplication.getInstance().progressON(this, message)
    }

    fun progressOFF(){
        BaseApplication.getInstance().progressOFF()
    }
}