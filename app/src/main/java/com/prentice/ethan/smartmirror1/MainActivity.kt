package com.prentice.ethan.smartmirror1

import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.prentice.ethan.smartmirror1.fragments.HomeFrag
import com.prentice.ethan.smartmirror1.fragments.InDevelopmentFrag
import com.prentice.ethan.smartmirror1.fragments.NotificationsFrag
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import com.prentice.ethan.smartmirror1.notifications.NLService
import android.content.Intent
import com.prentice.ethan.smartmirror1.fragments.FragInteractionListeners
import com.prentice.ethan.smartmirror1.notifications.NRService


class MainActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragInteractionListeners {

    private val fragmentManager = supportFragmentManager

    private lateinit var config: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        config = getSharedPreferences("config", 0)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Add MainFragment to FrameLayout
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.fragment_frame, HomeFrag())
        transaction.commit()
        toolbar.title = "Home"


        // Start background services
        val intentNLService = Intent(applicationContext, NLService::class.java)
        startService(intentNLService)
        val intentNRService = Intent(applicationContext, NRService::class.java)
        startService(intentNRService)


        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onFragmentInteraction(uri: Uri) {
        // Do nothing
    }

    @SuppressLint("CommitTransaction")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation item clicks
        when (item.itemId) {
            R.id.nav_home -> {
                val mirrorFrag = HomeFrag()
                switchFrag(R.id.fragment_frame, mirrorFrag)
            }
            R.id.nav_notif -> {
                val notifFrag = NotificationsFrag()
                switchFrag(R.id.fragment_frame, notifFrag)
            }
            else -> {
                val inDevFrag = InDevelopmentFrag()
                switchFrag(R.id.fragment_frame, inDevFrag)
            }
        }

        toolbar.title = item.title
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun switchFrag(id: Int, frag: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(id, frag)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
