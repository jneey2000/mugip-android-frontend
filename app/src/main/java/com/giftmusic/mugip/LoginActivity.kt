package com.giftmusic.mugip

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

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
                    val requestActivity : ActivityResultLauncher<Intent> = registerForActivityResult(
                        startActivityForResult()
                    )
                }
                R.id.btn_login_email -> {}
                R.id.btn_signup -> {}
            }
        }
    }

    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패", error)
        }
        else if (token != null) {
            Log.i(TAG, "로그인 성공 ${token.accessToken}")
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