package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
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
        val passwordConfirmInput = findViewById<EditText>(R.id.sign_up_password_confirm)
        val passwordConfirmString = findViewById<TextView>(R.id.sign_up_password_confirm_string)
        val nicknameInput = findViewById<EditText>(R.id.sign_up_nickname)
        val emailInput = findViewById<EditText>(R.id.sign_up_email)
        val emailDuplicateString = findViewById<TextView>(R.id.sign_up_email_duplicate_string)

        emailDuplicateString.visibility = View.INVISIBLE
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus && emailInput.text.isNotEmpty()){
                val duplicateEmail = checkDuplicateEmail(emailInput.text.toString())
                emailDuplicateString.visibility = View.VISIBLE
                if(duplicateEmail){
                    emailDuplicateString.text = "이미 가입된 이메일입니다."
                    emailDuplicateString.setTextColor(Color.RED)
                } else{
                    emailDuplicateString.text = "사용가능한 이메일입니다."
                    emailDuplicateString.setTextColor(ContextCompat.getColor(this, R.color.primary))
                }
            } else if(emailInput.text.isEmpty()){
                emailDuplicateString.visibility = View.INVISIBLE
            }
        }

        passwordConfirmString.visibility = View.INVISIBLE
        passwordConfirmInput.addTextChangedListener {
            confirmPassword -> when(confirmPassword!!.length){
                0 -> {
                    passwordConfirmString.visibility = View.INVISIBLE
                }
                else -> {
                    passwordConfirmString.visibility = View.VISIBLE
                    if(confirmPassword.toString() == passwordInput.text.toString()){
                        passwordConfirmString.text = "비밀번호 확인이 완료되었습니다."
                        passwordConfirmString.setTextColor(ContextCompat.getColor(this, R.color.primary))
                    } else {
                        passwordConfirmString.text = "비밀번호가 일치하지 않습니다."
                        passwordConfirmString.setTextColor(Color.RED)
                    }
                }
            }
        }

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
                } else{
                    showSuccessToSignUpDialog()
                }
            }
        }
    }

    // 이메일 중복 체크
    private fun checkDuplicateEmail(email : String): Boolean {
        var isDuplicated = false
        val checkEmailRequest = GlobalScope.launch {
            val url = URL(BuildConfig.server_url + "/user/email/check?email=" + email)
            val conn = url.openConnection() as HttpURLConnection
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
                            if(responseJson.has("exists")){
                                isDuplicated = true
                            }
                        }
                    }


                }
            }
            catch (e : Exception){
                Log.e("Check duplicate email error", e.message!!)
            }
            finally {
                conn.disconnect()
            }
        }
        runBlocking {
            checkEmailRequest.join()
        }
        return isDuplicated
    }

    // 로그인 성공할 때
    private fun showSuccessToSignUpDialog(){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("회원가입 성공")
            .setMessage("회원가입에 성공했습니다.")
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
                finish()
            }
        val dialog = dialogBuilder.create()
        dialog.show()
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