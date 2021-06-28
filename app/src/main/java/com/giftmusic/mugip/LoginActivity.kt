package com.giftmusic.mugip

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val naverLoginButton : Button = findViewById(R.id.btn_login_naver)
        val googleLoginButton : Button = findViewById(R.id.btn_login_google)
        val emailLoginButton : Button = findViewById(R.id.btn_login_email)
        val signUpButton : Button = findViewById(R.id.btn_signup)
        
        // 네이버 로그인
        naverLoginButton.setOnClickListener(LoginButtonListener())
    }

    inner class LoginButtonListener : View.OnClickListener{
        override fun onClick(v: View?) {
            when(v?.id){
                R.id.btn_login_naver -> {
                    val mOauthLoginModule = OAuthLogin.getInstance()
                    mOauthLoginModule.init(
                        applicationContext,
                        BuildConfig.Naver_Client_ID,
                        BuildConfig.Naver_Client_Secret,
                        BuildConfig.Naver_Client_Name
                    )


                    @SuppressLint("HandlerLeak")
                    val mOAuthLoginHandler : OAuthLoginHandler = object : OAuthLoginHandler(){
                        override fun run(success: Boolean){
                            if(success){

                            } else {
                                val errorCode: String = mOauthLoginModule.getLastErrorCode(applicationContext).code
                                val errorDesc = mOauthLoginModule.getLastErrorDesc(applicationContext)

                                Toast.makeText(
                                    baseContext, "errorCode:" + errorCode
                                            + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    mOauthLoginModule.startOauthLoginActivity(this@LoginActivity, mOAuthLoginHandler)
                }

                R.id.btn_login_google -> {}
                R.id.btn_login_email -> {}
                R.id.btn_signup -> {}
            }
        }
    }
}