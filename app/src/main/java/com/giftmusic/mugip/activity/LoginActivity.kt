package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.giftmusic.mugip.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility.getKeyHash
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        // 자동 앱 로그인(카카오)
        if(AuthApiClient.instance.hasToken()){
            UserApiClient.instance.accessTokenInfo{_, error ->
                if(error == null){
                    moveToMainActivity()
                }
            }
        }

        // 구글 로그인
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId().requestEmail().requestProfile().build()

        // 구글 자동 로그인
        val gsa = GoogleSignIn.getLastSignedInAccount(this)
        if(gsa != null){
            moveToMainActivity()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        // 버튼 연결
        val kakaoLoginButton : Button = findViewById(R.id.btn_login_kakao)
        val googleLoginButton : Button = findViewById(R.id.btn_login_google)
        val emailLoginButton : Button = findViewById(R.id.btn_login_email)
        val signUpButton : Button = findViewById(R.id.btn_signup)

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
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, 9001)
                }
                R.id.btn_login_email -> {
                    val intent = Intent(this@LoginActivity, LoginActivityEmail::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                    startActivity(intent)
                }
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

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUIWithGoogle(currentUser)
    }
    private fun updateUIWithGoogle(user: FirebaseUser?){
        if(user != null){
            moveToMainActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                signInWithGoogle()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun signInWithGoogle() {
        moveToMainActivity()
    }
}