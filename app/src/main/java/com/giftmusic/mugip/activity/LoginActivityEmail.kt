package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

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
                val signupRequest = GlobalScope.launch {
                    val url = URL(BuildConfig.server_url + "/user/signin")
                    val conn = url.openConnection() as HttpURLConnection
                    try {
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-Type", "application/json; utf-8")
                        conn.setRequestProperty("Accept", "application/json")
                        conn.doOutput = true

                        val requestJson = HashMap<String, String>()
                        requestJson["username"] = loginID.text.toString()
                        requestJson["password"] = loginPW.text.toString()

                        conn.outputStream.use { os ->
                            val input: ByteArray =
                                Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                            os.write(input, 0, input.size)
                        }

                        BufferedReader(
                            InputStreamReader(conn.inputStream, "utf-8")
                        ).use { br ->
                            val response = StringBuilder()
                            var responseLine: String? = null
                            while (br.readLine().also { responseLine = it } != null) {
                                response.append(responseLine!!.trim { it <= ' ' })
                            }
                            println(response.toString())
                            moveToMainActivity()
                        }
                    }
                    catch (e : Exception){
                        Log.e("Sign in error", e.message!!)
                        showFailToLoginDialog()
                    }
                    finally {
                        conn.disconnect()
                    }
                }
                runBlocking {
                    signupRequest.join()
                }
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