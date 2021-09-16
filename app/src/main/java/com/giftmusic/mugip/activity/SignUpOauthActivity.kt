package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import android.view.MotionEvent




class SignUpOauthActivity() : AppCompatActivity() {
    private var duplicatedNickname = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_oauth)

        val bundle = intent.extras
        val email = bundle!!.getString("email", "")
        val token = bundle.getString("token", "")
        val provider = bundle.getString("provider", "google")

        val oauthActivityBackground = findViewById<LinearLayout>(R.id.oauth_nickname_activity_background)
        val nicknameInput = findViewById<EditText>(R.id.oauth_nickname)
        val duplicateNicknameString = findViewById<TextView>(R.id.duplicate_nickname_string)
        val decideNicknameButton = findViewById<Button>(R.id.decide_nickname_button)

        duplicateNicknameString.visibility = View.INVISIBLE
        nicknameInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                val nickname = nicknameInput.text.toString()
                Log.d("Current Nickname", nickname)
                when {
                    checkDuplicateNickname(nickname) -> {
                        duplicateNicknameString.text = "중복된 닉네임입니다."
                        duplicateNicknameString.setTextColor(Color.RED)
                        duplicateNicknameString.visibility = View.VISIBLE
                        duplicatedNickname = true
                    }
                    nicknameInput.text.isNotEmpty() -> {
                        duplicateNicknameString.text = "사용가능한 닉네임입니다."
                        duplicateNicknameString.setTextColor(resources.getColor(R.color.primary, null))
                        duplicateNicknameString.visibility = View.VISIBLE
                        duplicatedNickname = false
                    }
                    else -> {
                        duplicateNicknameString.visibility = View.INVISIBLE
                    }
                }
            }
        }

        oauthActivityBackground.setOnFocusChangeListener{ _, hasFocus ->
            if(hasFocus){
                nicknameInput.clearFocus()
            }
        }

        decideNicknameButton.setOnClickListener {
            if(!duplicatedNickname){
                decideNickname(email, token, provider, nicknameInput.text.toString())
            }
        }
    }

    private fun checkDuplicateNickname(nickname : String) : Boolean {
        var isDuplicated = false
        var errorCode = -1
        val signupRequest = GlobalScope.launch {
            val url = URL(BuildConfig.server_url + "/user/check/nickname?nickname=" + nickname)
            val conn = url.openConnection() as HttpURLConnection
            val prefManager = this@SignUpOauthActivity.getPreferences(0)

            try {
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doInput = true

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            Log.d("receive data", responseJson.toString())
                            isDuplicated = responseJson.has("exists") && responseJson.get("exists") == "true"
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
        }
        runBlocking {
            signupRequest.join()
        }
        return isDuplicated
    }

    private fun decideNickname(email: String, token: String, provider: String, nickname: String){
        var loginFailed = true
        var errorCode = -1
        var userID = "-1"
        val signupRequest = GlobalScope.launch {
            val url = URL(BuildConfig.server_url + "/user/signup/oauth")
            val conn = url.openConnection() as HttpURLConnection
            val prefManager = this@SignUpOauthActivity.getPreferences(0)

            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true

                val requestJson = HashMap<String, String>()
                requestJson["Email"] = email
                requestJson["Uid"] = token
                requestJson["Provider"] = provider

                conn.outputStream.use { os ->
                    val input: ByteArray =
                        Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                    os.flush()
                }

                Log.d("statue code", conn.responseCode.toString())
                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            Log.d("receive data", responseJson.toString())
                            if(responseJson.has("user_id")){
                                loginFailed = false
                                val editor = prefManager.edit()
                                userID = responseJson["user_id"].toString()
                                editor.putString("user_id", responseJson["user_id"].toString()).apply()
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
        }
        runBlocking {
            signupRequest.join()
        }

        if(!loginFailed){
            showFailToSignUpDialog(errorCode)
        }
    }

    // 회원가입 실패할 때
    private fun showFailToSignUpDialog(errorCode: Int){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("회원가입 실패")
            .setMessage("회원가입에 실패했습니다.($errorCode)")
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    // 닉네임 창 말고 다른곳 클릭할 때
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}