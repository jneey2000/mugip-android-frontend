package com.giftmusic.mugip.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.OtherUser
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class MainActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private val otherUsers = ArrayList<OtherUser>() // 다른 사용자를 담기 위한 배열
    private lateinit var map : GoogleMap // 구글 지도 객체
    private lateinit var mLayout : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면 계속 켜져 있게
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.layout_main)
        

        // 지도 Fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        otherUsers.add(
            OtherUser(
                "https://raw.githubusercontent.com/Gift-Music/mugip-android-frontend/MVP/Jeongin/test_assets/user_1.png?token=ACQXZAY2XLEHCBD5Z7CXWDDA6DPJ2",
                LatLng(37.299914556000154, 126.8410831016941)
            )
        )

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
        openProfileActivityButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).putExtra("initialFragment", 0)
            startActivity(intent)
        }
        openPlayListActivityButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).putExtra("initialFragment", 1)
            startActivity(intent)

        }

        // 알람 메뉴 버튼
        val openNotificationActivityButton = findViewById<ImageView>(R.id.notification_button_main)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
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