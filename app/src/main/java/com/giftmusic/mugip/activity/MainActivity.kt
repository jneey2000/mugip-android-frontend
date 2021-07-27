package com.giftmusic.mugip.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.OtherUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking as runBlocking1


class MainActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private val otherUsers = ArrayList<OtherUser>() // 다른 사용자를 담기 위한 배열
    private val otherUserMarkers = ArrayList<MarkerOptions>() // 다른 사용자의 마커를 담기 위한 배열
    private lateinit var map : GoogleMap // 구글 지도 객체
    private lateinit var mLayout : View // 전체 레이아웃 객체
    private lateinit var currentLocation : Location // 현재 위치 객체
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.layout_main)
        
        otherUsers.add(
            OtherUser(
                "https://raw.githubusercontent.com/Gift-Music/mugip-android-frontend/MVP/Jeongin/test_assets/user_1.png?token=ACQXZAY2XLEHCBD5Z7CXWDDA6DPJ2",
                LatLng(37.299914556000154, 126.8410831016941)
            )
        )

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()

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

        // 하단 메뉴 버튼
        val openProfileActivityButton = findViewById<ImageView>(R.id.ic_profile)
        val openPlayListActivityButton = findViewById<ImageView>(R.id.ic_playlist)
        val openMapActivityButton = findViewById<ImageView>(R.id.center_button)
        openProfileActivityButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).putExtra("initialFragment", 0)
            startActivity(intent)
        }
        openPlayListActivityButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).putExtra("initialFragment", 1)
            startActivity(intent)

        }
        openMapActivityButton.setOnClickListener {
            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            addMarker()
        }


        // 알람 메뉴 버튼
        val openNotificationActivityButton = findViewById<ImageView>(R.id.notification_button_main)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }
    }


    // 지도 준비 후 동작
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val circleOption = CircleOptions().center(latLng).fillColor(Color.parseColor("#556B63FF")).radius(2000.0).strokeWidth(0f)
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        googleMap.addCircle(circleOption)

        addMarker()
    }

    // 현재 위치 fetch
    private fun fetchLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener {
            currentLocation = it
            val supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
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
                    val conn = url.openConnection() as HttpURLConnection;
                    conn.doInput = true; // 서버로 부터 응답 수신
                    conn.connect();

                    val inputStream: InputStream = conn.inputStream; // InputStream 값 가져오기
                    bitmap = BitmapFactory.decodeStream(inputStream)
                } catch (e: FileNotFoundException){
                    val markerImage = ResourcesCompat.getDrawable(resources,
                        R.drawable.user_1, null) as BitmapDrawable
                    bitmap = Bitmap.createScaledBitmap(markerImage.bitmap, 50, 50, false)
                }

                val markerOptions = MarkerOptions()
                markerOptions.position(it.location)
                markerOptions.draggable(true)
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                otherUserMarkers.add(markerOptions)
            }
        }
        runBlocking1 {
            refreshOtherUser.join()
        }
        Log.d("Count of other users", otherUserMarkers.size.toString())
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

}