package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.fragment.DiggingFeedFragment
import com.giftmusic.mugip.fragment.MainFragment
import com.giftmusic.mugip.fragment.ProfileFragment
import com.giftmusic.mugip.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, CoroutineScope {
    private lateinit var job: Job
    private lateinit var userNameTextView : TextView
    private lateinit var userProfileImageView: ImageView
    private var user : User? = null

    private lateinit var openProfileActivityButton : ImageView
    private lateinit var openPlayListActivityButton : ImageView
    private lateinit var openMapActivityButton : ImageView

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_main)
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        supportFragmentManager.commit {
            add(R.id.content_main, MainFragment())
        }

        // 하단 메뉴 버튼
        openProfileActivityButton = findViewById(R.id.ic_profile)
        openPlayListActivityButton = findViewById(R.id.ic_playlist)
        openMapActivityButton = findViewById(R.id.center_button)
        openProfileActivityButton.setOnClickListener {
            supportFragmentManager.commit {
                if(supportFragmentManager.findFragmentById(R.id.content_main)!! is MainFragment && user != null){
                    openMapActivityButton.isSelected = false
                    openPlayListActivityButton.isSelected = false
                    openProfileActivityButton.isSelected = true
                    add(R.id.content_main, ProfileFragment(user!!))
                } else if(supportFragmentManager.findFragmentById(R.id.content_main)!! !is ProfileFragment && user != null){
                    openMapActivityButton.isSelected = false
                    openPlayListActivityButton.isSelected = false
                    openProfileActivityButton.isSelected = true
                    replace(R.id.content_main, ProfileFragment(user!!))
                }
            }
        }
        openPlayListActivityButton.setOnClickListener {
            supportFragmentManager.commit {
                if(supportFragmentManager.findFragmentById(R.id.content_main)!! is MainFragment && user != null){
                    openMapActivityButton.isSelected = false
                    openPlayListActivityButton.isSelected = true
                    openProfileActivityButton.isSelected = false
                    add(R.id.content_main, DiggingFeedFragment(user!!))
                } else if(supportFragmentManager.findFragmentById(R.id.content_main)!! !is DiggingFeedFragment && user != null){
                    openMapActivityButton.isSelected = false
                    openPlayListActivityButton.isSelected = true
                    openProfileActivityButton.isSelected = false
                    replace(R.id.content_main, DiggingFeedFragment(user!!))
                }
            }
        }
        openMapActivityButton.setOnClickListener {
            supportFragmentManager.commit {
                if(supportFragmentManager.findFragmentById(R.id.content_main)!! is MainFragment){
                    (supportFragmentManager.findFragmentById(R.id.content_main)!! as MainFragment).fetchLocation()
                    openMapActivityButton.isSelected = true
                    openPlayListActivityButton.isSelected = false
                    openProfileActivityButton.isSelected = false
                } else{
                    remove(supportFragmentManager.findFragmentById(R.id.content_main)!!)
                    openMapActivityButton.isSelected = true
                    openPlayListActivityButton.isSelected = false
                    openProfileActivityButton.isSelected = false
                }
            }
        }
        openMapActivityButton.isSelected = true
        openPlayListActivityButton.isSelected = false
        openProfileActivityButton.isSelected = false
        // 사용자 정보 로딩
        userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.nav_bar_user_name)
        userProfileImageView = navigationView.getHeaderView(0).findViewById(R.id.nav_bar_profile_image)
        getMyInformation()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if(supportFragmentManager.findFragmentById(R.id.content_main)!! is MainFragment){
                super.onBackPressed()
            } else {
                supportFragmentManager.commit {
                    remove(supportFragmentManager.findFragmentById(R.id.content_main)!!)
                    openMapActivityButton.isSelected = true
                    openPlayListActivityButton.isSelected = false
                    openProfileActivityButton.isSelected = false
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        when(item.itemId){
            R.id.logout_button -> logoutButtonClicked()
        }
        return true
    }

    private fun getMyInformation(){
        progressOn("사용자 정보 불러오는 중...")
        var loadingFailed = true
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        var errorMessage = ""

        launch {
            val url = URL(BuildConfig.server_url + "/user/")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Basic ${prefManager.getString("access_token", "")}")
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            if(responseJson.has("user_id") && responseJson.has("user_name") &&
                                responseJson.has("email") && responseJson.has("user_nickname")){
                                user = User(
                                    responseJson.getString("user_id"),
                                    responseJson.getString("user_nickname"),
                                    responseJson.getString("user_name"),
                                    responseJson.getString("email"),
                                    null
                                )
                                loadingFailed = false
                                if(responseJson.has("profile_image")){
                                    val imageURL = URL(responseJson.getString("profile_image"))
                                    user!!.profileImage = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream())
                                }

                                // 사용자에 대한 알림을 받을 수 있도록 FCM 구독
                                Firebase.messaging.subscribeToTopic(user!!.userID).addOnCompleteListener {
                                    Log.d("FCM topic", user!!.userID)
                                }
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
                Log.e("fetch user information error", e.toString())
                Log.e("fetch user information error", e.javaClass.kotlin.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Dispatchers.Main){
                progressOFF()
                if(!loadingFailed && user != null){
                    userNameTextView.text = user!!.nickname
                    if(user!!.profileImage != null){
                        userProfileImageView.setImageBitmap(user!!.profileImage)
                    }
                } else if(errorMessage.isNotEmpty()){
                    showFailDialog("사용자 정보 로딩 실패", errorMessage)
                }
            }
        }
    }


    private fun logoutButtonClicked(){
        progressOn("로그아웃 중...")
        var logoutFailed = true
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        var errorMessage = ""
        launch {
            val url = URL(BuildConfig.server_url + "/user/logout")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Basic ${prefManager.getString("access_token", "")}")
                conn.doOutput = true
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val requestJson = HashMap<String, String>()

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
                            if(responseJson.getBoolean("successed")){
                                logoutFailed = false
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
                Log.e("Logout error", e.javaClass.kotlin.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Dispatchers.Main){
                progressOFF()
                if(!logoutFailed){
                    moveToLoginActivity()

                } else if(errorMessage.isNotEmpty()){
                    showFailDialog("회원가입 실패", errorMessage)
                }
            }
        }
    }

    private fun showFailDialog(title: String, errorMessage: String){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(errorMessage)
            .setPositiveButton("뒤로 가기") { _: DialogInterface, _: Int ->
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveToLoginActivity(){
        Log.d("Back", "Back to Login!")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
        finish()
    }
    
}