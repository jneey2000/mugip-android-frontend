package com.giftmusic.mugip

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private var map : GoogleMap? = null
    private var currentLocation : Circle? = null

    private val locationRequestCode = 2001
    private val locationUpdateInterval = 1L
    private val locationUpdateIntervalFastest : Long = 0.5.toLong()


    private val permissionRequestCode = 100
    var needRequest = false

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var currentPosition: LatLng
    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if(locationList.isNotEmpty()){
                location = locationList[locationList.size-1]
                currentPosition = LatLng(location.latitude, location.longitude)
                setCurrentLocation(location, "현재 위치", "마커 내용")
            }
        }
    }

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var location: Location
    private lateinit var mLayout : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면 계속 켜져 있게
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.layout_main)

        // 위치 권한 요청
        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(locationUpdateInterval).setFastestInterval(locationUpdateIntervalFastest)
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        // 위치 얻기
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        // 위치 수신이 안될때를 대비해 default 위치로 이동
        setDefaultLocation()
        // 권한 확인
        val hasFinePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        // 권한이 있으면 사용자 위치로 이동
        if(hasFinePermission && hasCoarsePermission){
            startLocationUpdates()
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPermissions[0])){
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("확인") {
                        ActivityCompat.requestPermissions(
                            this,
                            requiredPermissions,
                            permissionRequestCode
                        )
                    }.show()
            } else {
                ActivityCompat.requestPermissions(this, requiredPermissions, permissionRequestCode)
            }
        }

        // 현재 위치로 이동할 수 있는 버튼 추가
        if(map != null){
            map!!.uiSettings.isMyLocationButtonEnabled = false
            val currentLocationButton = findViewById<ImageView>(R.id.ic_location).setOnClickListener {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }


    private fun startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            showDialogForLocationServiceSetting()
        } else {
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음")
                return
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
            if (checkPermission() && map != null) map!!.isMyLocationEnabled = true
        }
    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            if(map != null){
                map!!.uiSettings.isMyLocationButtonEnabled = false;
            }
        }
    }


    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop : call stopLocationUpdates")
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }


    fun setCurrentLocation(location: Location, markerTitle: String?, markerSnippet: String?) {
        if(currentLocation != null){
            currentLocation!!.remove()
        }
        val currentLatLng = LatLng(location.latitude, location.longitude)
        val circleOption = CircleOptions().center(currentLatLng).fillColor(Color.parseColor("#556B63FF")).radius(100.0).strokeWidth(0f)
        if(map!=null){
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16F)
            map!!.moveCamera(cameraUpdate)
            currentLocation = map!!.addCircle(circleOption)
        }
    }


    private fun setDefaultLocation() {
        //디폴트 위치, Seoul
        val defaultLocation = LatLng(37.29685208641291, 126.83724122364636)
        if(currentLocation != null){
            currentLocation!!.remove()
        }
        val circleOption = CircleOptions().center(defaultLocation).fillColor(Color.parseColor("#556B63FF")).radius(100.0).strokeWidth(0f)

        if(map!=null){
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, 16F)
            map!!.moveCamera(cameraUpdate)
            currentLocation = map!!.addCircle(circleOption)
        }
    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return hasCoarseLocationPermission && hasFineLocationPermission
    }


    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String?>, grandResults: IntArray
    ) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults)
        if (permsRequestCode == permissionRequestCode && grandResults.size == requiredPermissions.size) {
            var checkResult = true
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                startLocationUpdates()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPermissions[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPermissions[1])
                ) {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Snackbar.LENGTH_INDEFINITE).setAction("확인") { finish() }.show()
                } else {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Snackbar.LENGTH_INDEFINITE).setAction("확인") { finish() }.show()
                }
            }
        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            """
            앱을 사용하기 위해서는 위치 서비스가 필요합니다.
            위치 설정을 수정하실래요?
            """.trimIndent()
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, locationRequestCode)
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            locationRequestCode ->
                if (checkLocationServicesStatus() && checkLocationServicesStatus()) {
                    Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                    needRequest = true
                    return
                }
        }
    }

}