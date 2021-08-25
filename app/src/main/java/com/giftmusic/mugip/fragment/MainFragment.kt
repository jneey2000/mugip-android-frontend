package com.giftmusic.mugip.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.giftmusic.mugip.R
import com.giftmusic.mugip.activity.AlarmActivity
import com.giftmusic.mugip.models.OtherUserOnMap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking as runBlocking1


class MainFragment : Fragment(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener {
    private val otherUsers = ArrayList<OtherUserOnMap>() // 다른 사용자를 담기 위한 배열
    private val otherUserMarkers = ArrayList<MarkerOptions>() // 다른 사용자의 마커를 담기 위한 배열
    private lateinit var map : GoogleMap // 구글 지도 객체
    private lateinit var currentLocation : Location // 현재 위치 객체
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_CODE = 1001
    private lateinit var mContext: Context
    private lateinit var mapView : MapView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        otherUsers.add(
            OtherUserOnMap(
                "https://raw.githubusercontent.com/Gift-Music/mugip-android-frontend/MVP/Jeongin/test_assets/user_1.png?token=ACQXZAY2XLEHCBD5Z7CXWDDA6DPJ2",
                LatLng(37.299914556000154, 126.8410831016941)
            )
        )
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        fetchLocation()

        val layout = inflater.inflate(R.layout.fragment_main, container, false)
        mapView = layout.findViewById(R.id.map) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // 상단 카테고리 버튼 동작
        val selectedCategoryButton = layout.findViewById<Button>(R.id.selected_category)
        val selectCategoryAllButton = layout.findViewById<Button>(R.id.select_category_all)
        val selectCategoryExerciseButton = layout.findViewById<Button>(R.id.select_category_exercise)
        val selectCategoryWorkButton = layout.findViewById<Button>(R.id.select_category_work)
        val selectCategoryReadingButton = layout.findViewById<Button>(R.id.select_category_reading)
        val selectCategoryDriveButton = layout.findViewById<Button>(R.id.select_category_drive)
        val selectCategoryTripButton = layout.findViewById<Button>(R.id.select_category_trip)
        val selectCategoryProgrammingButton = layout.findViewById<Button>(R.id.select_category_programming)
        val selectCategoryShowerButton = layout.findViewById<Button>(R.id.select_category_shower)
        selectedCategoryButton.setOnClickListener(CategoryButtonListener())
        selectCategoryAllButton.setOnClickListener(CategoryButtonListener())
        selectCategoryExerciseButton.setOnClickListener(CategoryButtonListener())
        selectCategoryWorkButton.setOnClickListener(CategoryButtonListener())
        selectCategoryReadingButton.setOnClickListener(CategoryButtonListener())
        selectCategoryDriveButton.setOnClickListener(CategoryButtonListener())
        selectCategoryTripButton.setOnClickListener(CategoryButtonListener())
        selectCategoryProgrammingButton.setOnClickListener(CategoryButtonListener())
        selectCategoryShowerButton.setOnClickListener(CategoryButtonListener())


        // 알람 메뉴 버튼
        val openNotificationActivityButton = layout.findViewById<ImageView>(R.id.notification_button_main)
        openNotificationActivityButton.setOnClickListener {
            val intent = Intent(activity, AlarmActivity::class.java)
            startActivity(intent)
        }

        return layout
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
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.content_main, OtherUserFragment()).addToBackStack(null).commit()
        return true
    }

    // 현재 위치 fetch
    fun fetchLocation(){
        if(ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity as Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
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
            val categoryScrollView = view!!.findViewById<HorizontalScrollView>(R.id.select_category_scrollview)
            val selectedCategoryButton = view!!.findViewById<Button>(R.id.selected_category)
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
                        selectedCategoryButton.text = view!!.findViewById<Button>(v?.id!!).text
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