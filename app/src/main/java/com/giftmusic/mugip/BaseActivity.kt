package com.giftmusic.mugip

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.giftmusic.mugip.activity.LoginActivity

abstract class BaseActivity : AppCompatActivity() {
    fun progressOn(){
        BaseApplication.getInstance().progressOn(this, null)
    }

    fun progressOn(message: String){
        BaseApplication.getInstance().progressOn(this, message)
    }

    fun progressOFF(){
        BaseApplication.getInstance().progressOFF()
    }

    fun showFailDialog(title: String, errorMessage: String){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(errorMessage)
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
                when(errorMessage){
                    "403"->{
                        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
                        val editor = prefManager.edit()
                        editor.putString("access_token", null).apply()
                        editor.putString("refresh_token", null).apply()
                        moveToLoginActivity()
                    }
                }
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    fun moveToLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        if(BaseApplication.getInstance().progressDialog != null && BaseApplication.getInstance().progressDialog!!.isShowing){
            BaseApplication.getInstance().progressDialog!!.dismiss()
        }
    }
}