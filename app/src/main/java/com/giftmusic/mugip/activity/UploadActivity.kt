package com.giftmusic.mugip.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.SearchSongListViewAdapter
import com.giftmusic.mugip.models.response.SearchMusicItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext

class UploadActivity : BaseActivity(), CoroutineScope {

    private lateinit var job : Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sharedSwitchChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        job = Job()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val addTagButton = findViewById<TextView>(R.id.add_tag_button)
        val addTagScrollView = findViewById<ScrollView>(R.id.tag_scroll_view)
        val uploadButton = findViewById<Button>(R.id.upload_button)
        val backButton = findViewById<ImageView>(R.id.back_to_profile)
        val alarmButton = findViewById<ImageView>(R.id.notification_button_profile)
        val shareLocationSwitch = findViewById<SwitchCompat>(R.id.share_location_switch)

        // 이전 Activity 에서 선택한 음악 정보 로딩
        val musicThumbnailView = findViewById<ImageView>(R.id.music_thumbnail)
        val musicTitleView = findViewById<TextView>(R.id.music_name)
        val musicArtistView = findViewById<TextView>(R.id.music_artist)
        if (intent.getParcelableExtra<Bitmap?>("thumbnail") != null){
            musicThumbnailView.setImageBitmap(intent.getParcelableExtra("thumbnail"))
        }
        musicTitleView.text = intent.getStringExtra("title")
        musicArtistView.text = intent.getStringExtra("artist")

        addTagButton.setOnClickListener {
            when(addTagScrollView.visibility){
                View.VISIBLE -> addTagScrollView.visibility = View.GONE
                View.GONE -> addTagScrollView.visibility = View.VISIBLE
                else -> {}
            }
        }
        backButton.setOnClickListener { finish() }
        alarmButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }
        
        val selectCategoryButton = arrayListOf<Button>(
            findViewById(R.id.select_category_exercise),
            findViewById(R.id.select_category_work),
            findViewById(R.id.select_category_reading),
            findViewById(R.id.select_category_drive),
            findViewById(R.id.select_category_trip),
            findViewById(R.id.select_category_programming),
            findViewById(R.id.select_category_shower)
        )
        selectCategoryButton.map {
            button -> button.setOnClickListener {
                addTagButton.text = button.text
                addTagButton.tag = button.id
            }
        }

        shareLocationSwitch.setOnCheckedChangeListener { _, isChecked -> sharedSwitchChecked = isChecked }

        uploadButton.setOnClickListener {
            if(addTagButton.text == "+"){
                showFailDialog("Tag 선택", "Tag 를 선택해주세요")
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { currentLocation ->
                progressOn("업로드 하는 중...")
                var uploadFailed = true
                var errorMessage = ""
                val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)

                launch {
                    val url = URL(BuildConfig.server_url + "/post/")
                    val conn = url.openConnection() as HttpURLConnection
                    try {
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-Type", "application/json; utf-8")
                        conn.setRequestProperty("Accept", "application/json")
                        conn.setRequestProperty("Authorization", "Basic ${prefManager.getString("access_token", "")}")
                        conn.doInput = true
                        conn.doOutput = true
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000

                        val requestJson = HashMap<String, Any>()
                        requestJson["title"] = intent.getStringExtra("title").toString()
                        requestJson["artist"] = intent.getStringExtra("artist").toString()
                        requestJson["thumbnailURL"] = intent.getStringExtra("thumbnailURL").toString()
                        requestJson["latitude"] = currentLocation.latitude
                        requestJson["longitude"] = currentLocation.longitude
                        requestJson["shareLocation"] = sharedSwitchChecked
                        requestJson["tag"] = addTagButton.text.substring(2, addTagButton.text.length).trim()

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
                                    val returnBody = conn.inputStream.bufferedReader().use(
                                        BufferedReader::readText)
                                    val responseJson = JSONObject(returnBody.trim())
                                    if(responseJson.getBoolean("success")){
                                        uploadFailed = false
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
                        Log.e("fetch search result error", e.toString())
                        Log.e("fetch search result error", e.javaClass.kotlin.toString())
                    }
                    finally {
                        conn.disconnect()
                    }
                    withContext(Dispatchers.Main){
                        progressOFF()
                        if(!uploadFailed){
                            showFailDialog("업로드 성공", "게시물 업로드 성공!")
                        } else if(errorMessage.isNotEmpty()){
                            showFailDialog("업로드 실패", errorMessage)
                        }
                    }
                }
            }
        }
    }
}