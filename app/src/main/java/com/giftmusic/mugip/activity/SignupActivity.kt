package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val idInput = findViewById<EditText>(R.id.sign_up_id)
        val passwordInput = findViewById<EditText>(R.id.sign_up_password)
        val nicknameInput = findViewById<EditText>(R.id.sign_up_nickname)
        val emailInput = findViewById<EditText>(R.id.sign_up_email)

        val signupButton = findViewById<Button>(R.id.btn_sign_up)
        signupButton.setOnClickListener {
            var signUpFailed = true
            var errorCode = -1
            val signupRequest = GlobalScope.launch {
                val url = URL(BuildConfig.server_url + "/user/signup")
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; utf-8")
                    conn.setRequestProperty("Accept", "application/json")
                    conn.doOutput = true
                    conn.doInput = true

                    val requestJson = HashMap<String, String>()
                    requestJson["nickname"] = nicknameInput.text.toString()
                    requestJson["username"] = idInput.text.toString()
                    requestJson["email"] = emailInput.text.toString()
                    requestJson["password"] = passwordInput.text.toString()

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
                                signUpFailed = false
                            }
                        }
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
                if(signUpFailed){
                    showFailToLoginDialog(errorCode)
                }
            }
        }
    }

    // 로그인 실패할 때
    private fun showFailToLoginDialog(errorCode: Int){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("회원가입 실패")
            .setMessage("회원가입에 실패했습니다.($errorCode)")
            .setPositiveButton("뒤로 가기", DialogInterface.OnClickListener(){
                    _: DialogInterface, _: Int ->
            })
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}