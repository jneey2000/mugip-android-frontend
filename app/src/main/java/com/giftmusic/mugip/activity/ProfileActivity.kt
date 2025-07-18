package com.giftmusic.mugip.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.ProfileFragmentStateAdapter
import com.giftmusic.mugip.models.MusicItem
import com.giftmusic.mugip.models.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

class ProfileActivity : BaseActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    lateinit var viewPager2 : ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("History", "Others")

    private lateinit var user: User
    private lateinit var historyList : ArrayList<MusicItem>
    private lateinit var followList : ArrayList<MusicItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        job = Job()
        getMyInformation()
        getMyHistoryInformation()
        getMyFollowInformation()

        val backButton = findViewById<ImageView>(R.id.back_to_map_from_profile)
        backButton.setOnClickListener {
            finish()
        }

        // 알람 메뉴 버튼
        val openNotificationActivityButton = findViewById<ImageView>(R.id.notification_button_profile)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }

        val settingButton = findViewById<Button>(R.id.setting_button)
        settingButton.setOnClickListener {

        }

        viewPager2 = findViewById(R.id.my_profile_tab_pager)
        tabLayout = findViewById(R.id.my_profile_tab_layout)
        viewPager2.adapter = ProfileFragmentStateAdapter(this, historyList, followList)
        TabLayoutMediator(tabLayout, viewPager2){
                tab, position -> tab.text = tabTextList[position]
        }.attach()
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
                            if(responseJson.has("user_id") &&
                                responseJson.has("email") && responseJson.has("nickname")){
                                user = User(
                                    responseJson.getString("user_id"),
                                    responseJson.getString("nickname"),
                                    responseJson.getString("email"),
                                    null
                                )
                                loadingFailed = false
                                if(responseJson.has("profile_image")){
                                    val imageURL = URL(responseJson.getString("profile_image"))
                                    user.profileImage = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream())
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
                if(!loadingFailed){
                    val userIDTextView = findViewById<TextView>(R.id.profile_id)
                    val userNickNameTextView = findViewById<TextView>(R.id.profile_nickname)
                    val userProfileImageView = findViewById<ImageView>(R.id.profile_image_view)

                    userIDTextView.text = user.email
                    userNickNameTextView.text = user.nickname
                    if(user.profileImage != null){
                        userProfileImageView.setImageBitmap(user.profileImage)
                    }
                } else if(errorMessage.isNotEmpty()){
                    showFailDialog("사용자 정보 로딩 실패", errorMessage)
                }
            }
        }
    }

    private fun getMyHistoryInformation(){
        progressOn("사용자 업로드 기록 불러오는 중...")
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        var errorMessage = ""
        historyList = ArrayList()

        launch {
            val url = URL(BuildConfig.server_url + "/user/history")
            val conn = url.openConnection() as HttpURLConnection
            val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            try {
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Basic ${prefManager.getString("access_token", "")}")
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                when(conn.responseCode){
                    200, 307 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            for (i in 0 until responseJson.getJSONArray("results").length()) {
                                val objects: JSONObject = responseJson.getJSONArray("results").getJSONObject(i)
                                historyList.add(
                                    MusicItem(
                                        objects.getString("thumbnailURL"),
                                        objects.getString("title"),
                                        objects.getString("artist"),
                                        objects.getString("tag"),
                                        LocalDateTime.parse(objects.getString("created_at"), dateTimeFormat)
                                    )
                                )
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
                Log.e("fetch user history list error", e.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Dispatchers.Main){
                progressOFF()
                if(errorMessage.isNotEmpty()){
                    showFailDialog("사용자 업로드 기록 로딩 실패", errorMessage)
                }
            }
        }
    }

    private fun getMyFollowInformation(){
        progressOn("사용자 팔로우 정보 불러오는 중...")
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        var errorMessage = ""
        followList = ArrayList()

        launch {
            val url = URL(BuildConfig.server_url + "/user/follow")
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
                    200, 307 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            Log.d("follow result", responseJson.toString())
                        }
                    }
                    else -> errorMessage = conn.responseCode.toString()
                }
            }
            catch (e : SocketTimeoutException){
                errorMessage = "연결 시간 초과 오류"
            }
            catch (e : Exception){
                Log.e("fetch user follow list error", e.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Dispatchers.Main){
                progressOFF()
                if(errorMessage.isNotEmpty()){
                    showFailDialog("사용자 팔로우 정보 로딩 실패", errorMessage)
                }
            }
        }
    }
}