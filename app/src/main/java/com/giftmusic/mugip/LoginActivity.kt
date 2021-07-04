package com.giftmusic.mugip

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.util.Utility

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val kakaoLoginButton : Button = findViewById(R.id.btn_login_kakao)
        val googleLoginButton : Button = findViewById(R.id.btn_login_google)
        val emailLoginButton : Button = findViewById(R.id.btn_login_email)
        val signUpButton : Button = findViewById(R.id.btn_signup)

        // 네이버 로그인
        kakaoLoginButton.setOnClickListener(LoginButtonListener())
        googleLoginButton.setOnClickListener(LoginButtonListener())
        emailLoginButton.setOnClickListener(LoginButtonListener())
        signUpButton.setOnClickListener(LoginButtonListener())
    }

    inner class LoginButtonListener : View.OnClickListener{
        override fun onClick(v: View?) {
            when(v?.id){
                R.id.btn_login_kakao -> {
                    if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
                        UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity, callback=kakaoCallback)

                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity, callback=kakaoCallback)
                    }
                }

                R.id.btn_login_google -> {

                }
                R.id.btn_login_email -> {}
                R.id.btn_signup -> {}
            }
        }
    }

    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패", error)
            showFailToLoginDialog()
        }
        else if (token != null) {
            moveToMainActivity()
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