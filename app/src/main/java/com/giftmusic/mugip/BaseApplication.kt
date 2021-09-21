package com.giftmusic.mugip

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatDialog
import com.kakao.sdk.common.KakaoSdk
import android.text.TextUtils
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView




class BaseApplication : Application() {
    var progressDialog : AppCompatDialog? = null
    companion object{
        lateinit var baseApplication : BaseApplication
        fun getInstance() : BaseApplication{
            return baseApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.kakao_api_key)
        baseApplication = this
    }

    fun progressON(activity : Activity?, message: String?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressSet(message)
        } else {

            progressDialog = AppCompatDialog(activity)
            progressDialog!!.setCancelable(false)
            progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog!!.setContentView(R.layout.dialog_loading)
            progressDialog!!.show()

        }

        val progressMessage = progressDialog!!.findViewById<TextView>(R.id.tv_progress_message)!!
        if (!TextUtils.isEmpty(message)) {
            progressMessage.text = message
        }
    }

    fun progressSet(message: String?) {
        if (progressDialog == null || !progressDialog!!.isShowing) {
            return
        }
        val progressMessage = progressDialog!!.findViewById<TextView>(R.id.tv_progress_message)
        if (!TextUtils.isEmpty(message)) {
            progressMessage!!.text = message
        }
    }

    fun progressOFF() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }
}