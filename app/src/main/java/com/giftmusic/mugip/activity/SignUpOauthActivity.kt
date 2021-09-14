package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

class SignUpOauthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_oauth)

        val nicknameInput = findViewById<EditText>(R.id.oauth_nickname)
        val duplicateNicknameString = findViewById<TextView>(R.id.duplicate_nickname_string)
        val decideNicknameButton = findViewById<Button>(R.id.decide_nickname_button)

        duplicateNicknameString.visibility = View.INVISIBLE
        nicknameInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){

            }
        }
    }

    private fun checkDuplicateNickname(nickname : String){

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
            if(loginFailed){
                showFailToSignUpDialog(errorCode)
            } else{
                loginFailed = true
                errorCode = -1
                val decideNicknameRequest = GlobalScope.launch {
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
                    if(loginFailed){
                        showFailToSignUpDialog(errorCode)
                    } else{
                        val intent = Intent(this@SignUpOauthActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    // 로그인 실패할 때
    private fun showFailToSignUpDialog(errorCode: Int){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("회원가입 실패")
            .setMessage("회원가입에 실패했습니다.($errorCode)")
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}