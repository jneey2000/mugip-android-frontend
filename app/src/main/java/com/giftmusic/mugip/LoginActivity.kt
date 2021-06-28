package com.giftmusic.mugip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
        googleLoginButton.setOnClickListener(LoginButtonListener())
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

                R.id.btn_login_google -> {
                    startActivityForResult(googleSignInIntent, RESULT_CODE)
                }
                R.id.btn_login_email -> {}
                R.id.btn_signup -> {}
            }
        }
    }

    private val googleSignInIntent by lazy {
        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestProfile().build()
        GoogleSignIn.getClient(applicationContext, googleSignInOption).signInIntent
    }

    companion object{
        const val RESULT_CODE = 10
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            result?.let {
                if (it.isSuccess) {
                    it.signInAccount?.displayName //이름
                    it.signInAccount?.email //이메일
                    Log.e("Value", it.signInAccount?.email!!)
                    // 기타 등등
                } else  {
                    Log.e("Value", "error")
                    // 에러 처리
                }
            }
        }
    }
}