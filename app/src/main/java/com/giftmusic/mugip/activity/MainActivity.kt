package com.giftmusic.mugip.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.giftmusic.mugip.R
import com.giftmusic.mugip.fragment.MainFragment
import com.giftmusic.mugip.fragment.ProfileFragment
import com.google.android.material.navigation.NavigationView


class MainActivity : FragmentActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var currentFragment : Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return true
    }

}