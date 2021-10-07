package com.giftmusic.mugip

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    fun progressOn(){
        BaseApplication.getInstance().progressOn(this, null)
    }

    fun progressOn(message: String){
        BaseApplication.getInstance().progressOn(this, message)
    }

    fun progressOFF(){
        BaseApplication.getInstance().progressOFF()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(BaseApplication.getInstance().progressDialog != null && BaseApplication.getInstance().progressDialog!!.isShowing){
            BaseApplication.getInstance().progressDialog!!.dismiss()
        }
    }
}