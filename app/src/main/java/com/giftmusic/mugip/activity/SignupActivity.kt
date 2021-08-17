package com.giftmusic.mugip.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import java.io.DataOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val idInput = findViewById<TextInputEditText>(R.id.id_signup)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_signup)
        val nicknameInput = findViewById<TextInputEditText>(R.id.nickname_signup)
        val emailInput = findViewById<TextInputEditText>(R.id.email_signup)

        val signupButton = findViewById<Button>(R.id.signup_button)
        signupButton.setOnClickListener {
            val signupRequest = GlobalScope.launch {
                val url = URL(BuildConfig.server_url + "/user/signup")
                val conn = url.openConnection() as HttpURLConnection
//                try{
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                val requestJson = HashMap<String, String>()
                requestJson["username"] = idInput.text.toString()
                requestJson["password"] = passwordInput.text.toString()
                requestJson["email"] = emailInput.text.toString()
                requestJson["usernickname"] = nicknameInput.text.toString()

                val writeStream = DataOutputStream(conn.outputStream)
                writeStream.writeBytes(Gson().toJson(requestJson))
                writeStream.flush()
                writeStream.close()
                Log.d("URL", url.toString())

//                } finally {
                conn.disconnect()
//                }
            }
            runBlocking {
                signupRequest.join()
            }
            Toast.makeText(this, "toast", Toast.LENGTH_SHORT).show()
        }
    }
}