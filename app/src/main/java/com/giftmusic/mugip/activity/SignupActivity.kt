package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext

class SignupActivity : BaseActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        job = Job()
        val idInput = findViewById<EditText>(R.id.sign_up_id)
        val passwordInput = findViewById<EditText>(R.id.sign_up_password)
        val passwordConfirmInput = findViewById<EditText>(R.id.sign_up_password_confirm)
        val passwordConfirmString = findViewById<TextView>(R.id.sign_up_password_confirm_string)
        val nicknameInput = findViewById<EditText>(R.id.sign_up_nickname)
        val nicknameDuplicateString = findViewById<TextView>(R.id.sign_up_nickname_duplicate_string)
        val emailInput = findViewById<EditText>(R.id.sign_up_email)
        val emailDuplicateString = findViewById<TextView>(R.id.sign_up_email_duplicate_string)
        var duplicateEmail = false

        emailDuplicateString.visibility = View.INVISIBLE
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus && emailInput.text.isNotEmpty()){
                progressOn()
                when {
                    Patterns.EMAIL_ADDRESS.matcher(emailInput.text.toString()).matches() -> {
                        emailDuplicateString.text = "사용가능한 이메일입니다."
                        emailDuplicateString.setTextColor(ContextCompat.getColor(this, R.color.primary))
                    }
                    else -> {
                        emailDuplicateString.text = "올바른 이메일 형식이 아닙니다."
                        emailDuplicateString.setTextColor(Color.RED)
                    }
                }
                checkDuplicateEmail(emailInput.text.toString())
            } else if(emailInput.text.isEmpty()){
                emailDuplicateString.visibility = View.INVISIBLE
            }
        }

        nicknameDuplicateString.visibility = View.INVISIBLE
        nicknameInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus && nicknameInput.text.isNotEmpty()){
                progressOn("닉네임 검증 중...")
                checkDuplicateNickname(nicknameInput.text.toString())
            } else if(nicknameInput.text.isEmpty()){
                nicknameDuplicateString.visibility = View.INVISIBLE
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
            if(emailInput.text.toString().isEmpty()){
                showDialog("이메일을 입력해주십시오.")
            } else if(duplicateEmail){
                showDialog("이미 가입된 이메일입니다.")
            } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput.text.toString()).matches()){
                showDialog("올바른 이메일 형식이 아닙니다.")
            } else if(idInput.text.toString().isEmpty()){
                showDialog("아이디를 입력해주십시오.")
            } else if(nicknameInput.text.toString().isEmpty()) {
                showDialog("닉네임을 입력해주십시오.")
            } else if(passwordInput.text.toString().isEmpty() || passwordConfirmInput.text.toString().isEmpty()){
                showDialog("비밀번호를 입력해주세요.")
            } else if(passwordInput.text.toString() != passwordConfirmInput.text.toString()){
                showDialog("비밀번호 확인 입력창에 동일한 비밀번호를 입력해주세요.")
            } else {
                var signUpFailed = true
                var errorCode = -1
                progressOn("회원가입 시도 중...")
                launch {
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
                    catch (e : SocketTimeoutException){
                        signUpFailed = true
                        duplicateEmail = true
                    }
                    catch (e : Exception){
                        Log.e("Sign in error", e.message!!)
                    }
                    finally {
                        conn.disconnect()
                    }

                    withContext(Main){
                        if(signUpFailed){
                            showFailToLoginDialog(errorCode)
                        } else{
                            showSuccessToSignUpDialog()
                        }
                    }
                }
            }
        }
    }

    // 이메일 중복 체크
    private fun checkDuplicateEmail(email : String){
        var isDuplicated = false
        var connected = true
        launch {
            val url = URL(BuildConfig.server_url + "/user/check/email?email=" + email)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            if(responseJson.has("exists") && responseJson.getBoolean("exists")){
                                isDuplicated = true
                            }
                        }
                    }


                }
            }
            catch (e : SocketTimeoutException){
                connected = false
            }
            catch (e : Exception){
                Log.e("Check duplicate email error", e.message!!)
            }
            finally {
                conn.disconnect()
            }

            withContext(Main){
                val emailDuplicateString = findViewById<TextView>(R.id.sign_up_email_duplicate_string)
                progressOFF()
                if (!connected){
                    val dialogBuilder = AlertDialog.Builder(this@SignupActivity)
                        .setTitle("중복 이메일 검증 실패")
                        .setMessage("서버 연결에 실패하여 이메일 검증에 실패하였습니다.")
                        .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int -> }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                    emailDuplicateString.text = "이메일 검증에 실패했습니다."
                    emailDuplicateString.setTextColor(Color.RED)

                } else if(isDuplicated){
                    emailDuplicateString.text = "이미 가입된 이메일입니다."
                    emailDuplicateString.setTextColor(Color.RED)
                } else if(!isDuplicated){
                    emailDuplicateString.text = "사용 가능한 이메일입니다."
                    emailDuplicateString.setTextColor(resources.getColor(R.color.primary))
                }
                emailDuplicateString.visibility = View.VISIBLE
            }
        }
    }

    // 닉네임 중복 체크
    private fun checkDuplicateNickname(nickName : String){
        var isDuplicated = false
        var connected = true
        launch {
            val url = URL(BuildConfig.server_url + "/user/check/nickname?nickname=" + nickName)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            if(responseJson.has("exists") && responseJson.getBoolean("exists")){
                                isDuplicated = true
                            }
                        }
                    }


                }
            }
            catch (e : SocketTimeoutException){
                connected = false
            }
            catch (e : Exception){
                Log.e("Check duplicate nickname error", e.message!!)
            }
            finally {
                conn.disconnect()
            }

            withContext(Main){
                val nicknameDuplicateString = findViewById<TextView>(R.id.sign_up_nickname_duplicate_string)
                progressOFF()
                if (!connected){
                    val dialogBuilder = AlertDialog.Builder(this@SignupActivity)
                        .setTitle("중복 닉네임 검증 실패")
                        .setMessage("서버 연결에 실패하여 닉네임 검증에 실패하였습니다.")
                        .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int -> }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                    nicknameDuplicateString.text = "닉네임 검증에 실패했습니다."
                    nicknameDuplicateString.setTextColor(Color.RED)

                } else if(isDuplicated){
                    nicknameDuplicateString.text = "이미 등록된 닉네임입니다."
                    nicknameDuplicateString.setTextColor(Color.RED)
                } else if(!isDuplicated){
                    nicknameDuplicateString.text = "사용 가능한 닉네임입니다."
                    nicknameDuplicateString.setTextColor(resources.getColor(R.color.primary))
                }
                nicknameDuplicateString.visibility = View.VISIBLE
            }
        }
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
        val dialogBuilder : AlertDialog.Builder = if (errorCode == -1){
            AlertDialog.Builder(this)
                .setTitle("회원가입 실패")
                .setMessage("회원가입에 실패했습니다.($errorCode)")
                .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
                }
        } else {
            AlertDialog.Builder(this)
                .setTitle("회원가입 실패")
                .setMessage("회원가입에 실패했습니다.($errorCode)")
                .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
                }
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    // 에러 팝업을 띄울 때
    private fun showDialog(message: String){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("회원가입 실패")
            .setMessage(message)
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}