package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

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
            .requestIdToken(getString(R.string.default_web_client_id)).build()

        // 구글 자동 로그인
        val gsa = GoogleSignIn.getLastSignedInAccount(this)
        if(gsa != null){
            moveToMainActivity()
        }
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_login)

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        // 버튼 연결
        val kakaoLoginButton : ImageButton = findViewById(R.id.btn_login_kakao)
        val googleLoginButton : LinearLayout = findViewById(R.id.btn_login_google)

        kakaoLoginButton.setOnClickListener(LoginButtonListener())
        googleLoginButton.setOnClickListener(LoginButtonListener())

        val loginButton = findViewById<Button>(R.id.btn_login_email)
        val loginID = findViewById<EditText>(R.id.email_login_id)
        val loginPW = findViewById<EditText>(R.id.email_login_password)
        loginButton.setOnClickListener{
            if(TextUtils.isEmpty(loginID.text) || !android.util.Patterns.EMAIL_ADDRESS.matcher(loginID.text as CharSequence).matches() || TextUtils.isEmpty(loginPW.text)) {
                showFailToLoginDialog(-1)
            } else {
                var loginFailed = true
                var errorCode = -1
                val signupRequest = GlobalScope.launch {
                    val url = URL(BuildConfig.server_url + "/user/login")
                    val conn = url.openConnection() as HttpURLConnection
//                    try {
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-Type", "application/json; utf-8")
                        conn.setRequestProperty("Accept", "application/json")
                        conn.doOutput = true
                        conn.doInput = true

                        val requestJson = HashMap<String, String>()
                        requestJson["Email"] = loginID.text.toString()
                        requestJson["Password"] = loginPW.text.toString()

                        conn.outputStream.use { os ->
                            val input: ByteArray =
                                Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                            os.write(input, 0, input.size)
                            os.flush()
                        }

                        when(conn.responseCode){
                            401 -> errorCode = 401
                            200 -> {
                                val inputStream = conn.inputStream
                                if(inputStream != null){
                                    val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                                    val responseJson = JSONObject(returnBody.trim())
                                    Log.d("received data", responseJson.toString())
                                    loginFailed = false
                                }
                            }
                        }
//                    }
//                    catch (e : Exception){
//                        Log.e("Sign in error", e.message!!)
//                    }
//                    finally {
                        conn.disconnect()
//                    }
                }
                runBlocking {
                    signupRequest.join()
                    if(loginFailed){
                        showFailToLoginDialog(errorCode)
                    }
                }
            }
        }
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
            }
        }
    }

    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e("로그인 실패", error.toString())
            showFailToLoginDialog(-1)
        }
        else if (token != null) {
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e(TAG, "사용자 정보 요청 실패", error)
                }
                else if (user != null) {
                    Log.i(TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
                    moveToMainActivity()
                }
            }
        }
    }

    // 로그인 실패할 때
    private fun showFailToLoginDialog(errorCode: Int){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("로그인 실패")
            .setMessage("로그인에 실패했습니다.($errorCode)")
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
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.idToken)
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