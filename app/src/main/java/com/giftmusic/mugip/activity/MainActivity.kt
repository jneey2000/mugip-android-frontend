package com.giftmusic.mugip.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.isDigitsOnly
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.fragment.MainFragment
import com.giftmusic.mugip.fragment.ProfileFragment
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, CoroutineScope {
    private lateinit var currentFragment : Fragment
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_main)
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        currentFragment = MainFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content_main, currentFragment).commit()

        // 하단 메뉴 버튼
        val openProfileActivityButton = findViewById<ImageView>(R.id.ic_profile)
        val openPlayListActivityButton = findViewById<ImageView>(R.id.ic_playlist)
        val openMapActivityButton = findViewById<ImageView>(R.id.center_button)
        openProfileActivityButton.setOnClickListener {
            if (currentFragment !is ProfileFragment){
                supportFragmentManager.beginTransaction().replace(R.id.content_main, ProfileFragment()).addToBackStack(null).commit()
            }
        }
        openPlayListActivityButton.setOnClickListener {

        }
        openMapActivityButton.setOnClickListener {
            if(currentFragment is MainFragment){
                (currentFragment as MainFragment).fetchLocation()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if(currentFragment is MainFragment){
                super.onBackPressed()
            } else {
                supportFragmentManager.beginTransaction().replace(R.id.content_main, ProfileFragment()).commit()
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
                    showFailToLogoutDialog(errorMessage)
                }
            }
        }
    }

    private fun showFailToLogoutDialog(errorMessage: String){
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("로그아웃 실패")
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