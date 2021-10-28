package com.giftmusic.mugip.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.isDigitsOnly
import com.auth0.android.jwt.JWT
import com.giftmusic.mugip.BaseActivity
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
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext

class LoginActivity : BaseActivity(), CoroutineScope {
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        // Coroutine scope 지정을 위한 job 선언
        job = Job()
        
        // 자동 로그인을 위한 token 로딩 및 로그인 시도
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        val accessToken = prefManager.getString("access_token", null)
        val refreshToken = prefManager.getString("refresh_token", null)
        if(accessToken != null && refreshToken != null){
            signInWithToken(accessToken, refreshToken)
        }

        // 레이아웃 및 스플래시 스크린
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_login)

        // 구글 로그인 옵션
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId().requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        // 버튼 연결(Oauth)
        val kakaoLoginButton : ImageButton = findViewById(R.id.btn_login_kakao)
        val googleLoginButton : LinearLayout = findViewById(R.id.btn_login_google)
        kakaoLoginButton.setOnClickListener(LoginButtonListener())
        googleLoginButton.setOnClickListener(LoginButtonListener())

        // 버튼 연결(Email/PW)
        val loginButton = findViewById<Button>(R.id.btn_login_email)
        val loginID = findViewById<EditText>(R.id.email_login_id)
        val loginPW = findViewById<EditText>(R.id.email_login_password)
 
        // 아이디 및 패스워드 입력 화면에서 키보드 자동 완성을 사용했을 때 
        loginID.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                loginID.setText(loginID.text.toString().trim())
            }
        }
        loginPW.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                loginPW.setText(loginPW.text.toString().trim())
            }
        }

        // 로그인 버튼 클릭했을 때 -> 이메일 형식에 맞는지 검증
        loginButton.setOnClickListener{
            if(TextUtils.isEmpty(loginID.text.toString()) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(loginID.text.toString() as CharSequence).matches() ||
                TextUtils.isEmpty(loginPW.text.toString())) {
                showDialog("이메일 입력 오류", "올바른 이메일 형식이 아닙니다.")
            } else {
                signInWithEmail(loginID.text.toString(), loginPW.text.toString())
            }
        }

        // 회원가입 창으로 이동
        val signUPButton = findViewById<Button>(R.id.btn_sign_up)
        signUPButton.setOnClickListener {
            val signupActivity = Intent(this, SignupActivity::class.java)
            startActivity(signupActivity)
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
            showDialog("카카오 로그인 오류", "카카오 계정 로그인에 실패했습니다.")
        }
        else if (token != null) {
            UserApiClient.instance.me { user, loginError ->
                if (loginError != null) {
                    Log.e(TAG, "사용자 정보 요청 실패", loginError)
                }
                else if (user != null) {
                    signInWithOauth(user.id.toString(), user.kakaoAccount?.email!!, "kakao")
                }
            }
        }
    }

    // 로그인 실패할 때
    private fun showDialog(dialogTitle: String, dialogMessage: String){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    // 소셜 로그인 사용자가 등록되어 있지 않을 때
    private fun showFailToOauthLoginDialog(email: String, token: String, provider: String){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("등록되지 않은 사용자")
            .setMessage("등록되어 있지 않는 계정입니다. 닉네임 생성 화면으로 이동합니다.")
            .setPositiveButton("닉네임 생성하기") { _: DialogInterface, _: Int ->
                val intent = Intent(this, SignUpOauthActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("token", token)
                intent.putExtra("provider", provider)
                startActivity(intent)
                finish()
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                progressOn("Google 로그인 중..")
                signInWithOauth(account.id!!, account.email!!, "google")
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun signInWithToken(accessToken: String, refreshToken: String){
        val jwtToken = JWT(accessToken)
        if(jwtToken.isExpired(100)){
            var refreshFailed = true
            var errorCode = -1
            val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
            val editor = prefManager.edit()
            launch {
                val url = URL(BuildConfig.server_url + "/user/token/refresh")
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; utf-8")
                    conn.setRequestProperty("Accept", "application/json")
                    conn.doOutput = true
                    conn.doInput = true

                    val requestJson = HashMap<String, String>()
                    requestJson["refresh_token"] = refreshToken
                    requestJson["access_token"] = accessToken

                    conn.outputStream.use { os ->
                        val input: ByteArray =
                            Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                        os.flush()
                    }

                    when(conn.responseCode){
                        200 -> {
                            val inputStream = conn.inputStream
                            if(inputStream != null){
                                val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                                val responseJson = JSONObject(returnBody.trim())
                                if(responseJson.has("access_token") && responseJson.has("refresh_token")){
                                    refreshFailed = false
                                    editor.putString("access_token", responseJson["access_token"].toString()).apply()
                                    editor.putString("refresh_token", responseJson["refresh_token"].toString()).apply()
                                }
                            }
                        }
                        else -> errorCode = conn.responseCode
                    }
                }
                catch (e : Exception){
                    Log.e("refresh token error", e.message!!)
                }
                finally {
                    conn.disconnect()
                }

                withContext(Main){
                    progressOFF()
                    if(refreshFailed){
                        when(errorCode){
                            401 -> showDialog("인증 오류", "인증 오류 $errorCode")
                        }
                    } else{
                        moveToMainActivity()
                    }
                }
            }
        } else {
            moveToMainActivity()
        }
    }

    private fun signInWithOauth(uid: String, email: String, provider: String) {
        var loginFailed = true
        var errorCode = -1
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        val editor = prefManager.edit()
        launch {
            val url = URL(BuildConfig.server_url + "/auth/login/oauth")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true

                val requestJson = HashMap<String, String>()
                requestJson["email"] = email
                requestJson["uid"] = uid
                requestJson["provider"] = provider

                conn.outputStream.use { os ->
                    val input: ByteArray =
                        Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                    os.flush()
                }

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            if(responseJson.has("access_token") && responseJson.has("refresh_token")){
                                loginFailed = false
                                editor.putString("access_token", responseJson["access_token"].toString()).apply()
                                editor.putString("refresh_token", responseJson["refresh_token"].toString()).apply()
                            }
                        }
                    }
                    else -> errorCode = conn.responseCode
                }
            }
            catch (e : Exception){
                Log.e("Sign in error", e.message!!)
            }
            finally {
                conn.disconnect()
            }

            withContext(Main){
                progressOFF()
                if(loginFailed){
                    when(errorCode){
                        409 -> showFailToOauthLoginDialog(email, uid, provider)
                        else -> showDialog("인증 오류", "인증 오류 $errorCode")
                    }
                } else{
                    moveToMainActivity()
                }
            }
        }
    }

    private fun signInWithEmail(email : String, password : String){
        progressOn("Loading...")
        var loginFailed = true
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        val editor = prefManager.edit()
        var errorMessage = ""
        launch {
            val url = URL(BuildConfig.server_url + "/auth/login")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val requestJson = HashMap<String, String>()
                requestJson["email"] = email
                requestJson["password"] = password

                conn.outputStream.use { os ->
                    val input: ByteArray =
                        Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                    os.flush()
                }

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            if(responseJson.has("access_token") && responseJson.has("refresh_token")){
                                loginFailed = false
                                editor.putString("access_token", responseJson["access_token"].toString()).apply()
                                editor.putString("refresh_token", responseJson["refresh_token"].toString()).apply()
                            }
                        }
                    }
                    else -> errorMessage = conn.responseCode.toString()
                }
            }
            catch (e : SocketTimeoutException){
                errorMessage = "연결 시간 초과 오류"
            }
            catch (e : Exception){
                Log.e("Sign in error", e.javaClass.kotlin.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Main){
                progressOFF()
                if(errorMessage.isNotEmpty()){
                    showDialog("인증 오류", "인증 오류 $errorMessage")
                } else if(!loginFailed){
                    moveToMainActivity()
                }
            }
        }
    }
}