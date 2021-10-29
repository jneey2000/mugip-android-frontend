package com.giftmusic.mugip.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.fragment.OtherUserFragment
import com.giftmusic.mugip.models.OtherUserOnMap
import com.giftmusic.mugip.models.SearchUser
import com.giftmusic.mugip.models.SearchUserItem
import com.giftmusic.mugip.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext


class MainActivity : BaseActivity(), CoroutineScope,
    OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var job: Job
    private lateinit var userNameTextView : TextView
    private lateinit var userProfileImageView: ImageView
    private var user : User? = null

    private lateinit var openProfileActivityButton : ImageView
    private lateinit var openPlayListActivityButton : ImageView
    private lateinit var openMapActivityButton : ImageView
    private lateinit var mapView: MapView
    private val REQUEST_CODE = 1001
    private val otherUsers = ArrayList<OtherUserOnMap>() // 다른 사용자를 담기 위한 배열
    private val otherUserMarkers = ArrayList<MarkerOptions>() // 다른 사용자의 마커를 담기 위한 배열
    private lateinit var map : GoogleMap // 구글 지도 객체
    private lateinit var currentLocation : Location // 현재 위치 객체
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById<MapView>(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // 상단 카테고리 버튼 동작
        val selectedCategoryButton = findViewById<Button>(R.id.selected_category)
        val selectCategoryAllButton = findViewById<Button>(R.id.select_category_all)
        val selectCategoryExerciseButton = findViewById<Button>(R.id.select_category_exercise)
        val selectCategoryWorkButton = findViewById<Button>(R.id.select_category_work)
        val selectCategoryReadingButton = findViewById<Button>(R.id.select_category_reading)
        val selectCategoryDriveButton = findViewById<Button>(R.id.select_category_drive)
        val selectCategoryTripButton = findViewById<Button>(R.id.select_category_trip)
        val selectCategoryProgrammingButton = findViewById<Button>(R.id.select_category_programming)
        val selectCategoryShowerButton = findViewById<Button>(R.id.select_category_shower)
        selectedCategoryButton.setOnClickListener(CategoryButtonListener())
        selectCategoryAllButton.setOnClickListener(CategoryButtonListener())
        selectCategoryExerciseButton.setOnClickListener(CategoryButtonListener())
        selectCategoryWorkButton.setOnClickListener(CategoryButtonListener())
        selectCategoryReadingButton.setOnClickListener(CategoryButtonListener())
        selectCategoryDriveButton.setOnClickListener(CategoryButtonListener())
        selectCategoryTripButton.setOnClickListener(CategoryButtonListener())
        selectCategoryProgrammingButton.setOnClickListener(CategoryButtonListener())
        selectCategoryShowerButton.setOnClickListener(CategoryButtonListener())

        // 검색창 event listener
        val searchEditText = findViewById<EditText>(R.id.user_search_edittext)
        searchEditText.setOnKeyListener { v, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                getSearchResult((v as EditText).text.toString())
            }
            true
        }
        // 알람 메뉴 버튼
        val openNotificationActivityButton = findViewById<ImageView>(R.id.notification_button_main)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }

        // 하단 메뉴 버튼
        openProfileActivityButton = findViewById(R.id.ic_profile)
        openPlayListActivityButton = findViewById(R.id.ic_playlist)
        openMapActivityButton = findViewById(R.id.center_button)
        openProfileActivityButton.setOnClickListener {

        }

        openPlayListActivityButton.setOnClickListener {

        }
        openMapActivityButton.setOnClickListener {

        }
        openMapActivityButton.isSelected = true
        openPlayListActivityButton.isSelected = false
        openProfileActivityButton.isSelected = false
        getMyInformation()
    }

    private fun getSearchResult(searchKeyword: String) {
        progressOn("사용자 검색 결과 불러오는 중...")
        var searchUserFailed = true
        var errorMessage = ""
        val searchResult = ArrayList<SearchUserItem>()
        launch {
            val url = URL(BuildConfig.server_url + "/search/user")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doInput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val requestJson = HashMap<String, String>()
                requestJson["nickname"] = searchKeyword

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
                            if (responseJson.has("users")){
                                val result = (responseJson.get("users") as ArrayList<HashMap<String, *>>)
                                for(item in result){
                                    searchResult.add(SearchUserItem(
                                        item["user_id"] as Int, item["email"] as String, item["nickname"] as String
                                    ))
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
                Log.e("fetch search result error", e.toString())
                Log.e("fetch search result error", e.javaClass.kotlin.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Dispatchers.Main){
                progressOFF()
                Log.d("result", searchResult.toString())
                if(!searchUserFailed && user != null){
                    for(item in searchResult){
                        Log.d("found", item.toString())
                    }
                } else if(errorMessage.isNotEmpty()){
                    showFailDialog("사용자 검색 실패", errorMessage)
                }
            }
        }
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
                                    responseJson.getString("user_nickname"),
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
                            if(responseJson.getBoolean("success")){
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
                when(errorMessage){
                    "403"->moveToLoginActivity()
                }
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveToLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
        finish()
    }

    // 지도 준비 후 동작
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false

        val latLng = LatLng(37.29685208641291, 126.83724122364636)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        addMarker()
        map.setOnMarkerClickListener(this)
        map.setOnMapClickListener {
            (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.window.decorView.applicationWindowToken, 0)
        }
        fetchLocation()
        // 지도 위치가 이동했을 때
        map.setOnCameraIdleListener {
            val visibleRegion = map.projection.visibleRegion.latLngBounds
            Log.d("map location(northeast)", "Location: (${visibleRegion.northeast.latitude}, ${visibleRegion.northeast.longitude})")
            Log.d("map location(southwest)", "Location: (${visibleRegion.southwest.latitude}, ${visibleRegion.southwest.longitude})")
            // 마커 업데이트 코드 삽입
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return true
    }

    // 현재 위치 fetch
    private fun fetchLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
        } else {
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener {
                currentLocation = it
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            }
        }
    }

    // 다른 사용자의 위치 marker
    private fun addMarker(){
        otherUserMarkers.clear()
        val refreshOtherUser = GlobalScope.launch {
            otherUsers.map {
                var bitmap : Bitmap
                try{
                    val url = URL(it.imageUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.doInput = true // 서버로 부터 응답 수신
                    conn.connect()

                    val inputStream: InputStream = conn.inputStream // InputStream 값 가져오기
                    bitmap = BitmapFactory.decodeStream(inputStream)
                } catch (e: FileNotFoundException){
                    val markerImage = ResourcesCompat.getDrawable(resources,
                        R.drawable.ic_profile, null) as BitmapDrawable
                    bitmap = Bitmap.createScaledBitmap(markerImage.bitmap, 50, 50, false)
                }

                val markerOptions = MarkerOptions()
                markerOptions.position(it.location)
                markerOptions.draggable(true)
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                otherUserMarkers.add(markerOptions)
            }
        }
        runBlocking {
            refreshOtherUser.join()
        }
        otherUserMarkers.map {
            map.addMarker(it)
        }
    }

    inner class CategoryButtonListener : View.OnClickListener{
        override fun onClick(v: View?) {
            val categoryScrollView = findViewById<HorizontalScrollView>(R.id.select_category_scrollview)
            val selectedCategoryButton = findViewById<Button>(R.id.selected_category)
            when(v?.id){
                R.id.selected_category ->{
                    if(categoryScrollView.visibility == View.VISIBLE){
                        categoryScrollView.visibility = View.INVISIBLE
                    } else{
                        categoryScrollView.visibility = View.VISIBLE
                    }
                }
                else -> {
                    if(categoryScrollView.visibility == View.VISIBLE){
                        selectedCategoryButton.text = findViewById<Button>(v?.id!!).text
                        categoryScrollView.visibility = View.INVISIBLE
                    }
                }
            }
        }

    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

}