package com.giftmusic.mugip

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.kakao.sdk.user.UserApiClient

class LoginActivityEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_email)

        val loginButton = findViewById<Button>(R.id.btn_login_email_2nd)
        val loginID = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.email_login_id)
        val loginPW = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.email_login_password)
        loginButton.setOnClickListener{
            if(TextUtils.isEmpty(loginID.text) || !android.util.Patterns.EMAIL_ADDRESS.matcher(loginID.text as CharSequence).matches() || TextUtils.isEmpty(loginPW.text)) {
                showFailToLoginDialog()
            } else {
                moveToMainActivity()
            }
        }
    }

    // 로그인 실패할 때
    private fun showFailToLoginDialog(){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("로그인 실패")
            .setMessage("로그인에 실패했습니다.")
            .setPositiveButton("뒤로 가기", DialogInterface.OnClickListener(){
                    _: DialogInterface, _: Int ->
            })
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}