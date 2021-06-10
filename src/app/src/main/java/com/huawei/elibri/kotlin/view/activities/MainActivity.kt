/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.huawei.agconnect.applinking.AGConnectAppLinking
import com.huawei.agconnect.applinking.AppLinking
import com.huawei.agconnect.applinking.AppLinking.AndroidLinkInfo
import com.huawei.agconnect.applinking.ShortAppLinking
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.iap.SubscriptionUtils.getPurchaseResult
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.service.model.Profile
import com.huawei.elibri.kotlin.service.repository.CloudDb
import com.huawei.elibri.kotlin.service.repository.CloudDbQueries
import com.huawei.elibri.kotlin.utility.Constants
import com.huawei.elibri.kotlin.utility.Util.isOnline
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.hmf.tasks.Task
import com.huawei.hms.iap.entity.OrderStatusCode

/**
 * Base activity for all the fragments and navigation drawer
 *
 * @author: lWX916345
 * @since: 02-11-2020
 */
class MainActivity : AppCompatActivity() {
    /**
     * To navigate between drawer classes
     */
    var navController: NavController? = null

    /**
     * For drawer menu
     */
    var drawer: DrawerLayout? = null
    /**
     * Cloud db zone object
     */
    protected var mCloudDBZone: CloudDBZone? = null
    private var agcLink: String? = null
    private var mAppBarConfiguration: AppBarConfiguration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_kotlin)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        initializeDrawer()
        isOnline(applicationContext)
        createAppLinking()
    }

    /**
     * Initializes drawer
     */
    private fun initializeDrawer() {
        AGConnectAppLinking.getInstance()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        navController = navHostFragment!!.navController
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_my_book, R.id.nav_bookmark, R.id.nav_premium,
            R.id.nav_profile, R.id.nav_invite, R.id.nav_logout).setDrawerLayout(drawer).build()
        NavigationUI.setupActionBarWithNavController(this, navController!!, mAppBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController!!)
        setUserData(navigationView)
        navController!!.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.nav_my_book, R.id.nav_bookmark, R.id.nav_premium, R.id.nav_profile -> drawer?.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_UNLOCKED
                )
                else -> drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        navigationView.menu.findItem(R.id.nav_invite)
            .setOnMenuItemClickListener { menuItem: MenuItem? ->
                shareLink()
                true
            }
        navigationView.menu.findItem(R.id.nav_logout)
            .setOnMenuItemClickListener { menuItem: MenuItem? ->
                signOut()
                true
            }
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Initialize app linking
     */
    private fun createAppLinking() {
        val builder = AppLinking.Builder()
            .setUriPrefix(Constants.DOMAIN_URI_PREFIX)
            .setDeepLink(Uri.parse(Constants.DEEP_LINK))
            .setAndroidLinkInfo(AndroidLinkInfo.Builder().build())
        builder.buildShortAppLinking()
            .addOnSuccessListener { shortAppLinking: ShortAppLinking ->
                agcLink = shortAppLinking.shortUrl.toString()
            }
            .addOnFailureListener { exception: Exception? ->
                Toast.makeText(
                    applicationContext,
                    "exception occured",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Sets user data in Navigation drawer
     *
     * @param navigationView : NavigationView
     */
    private fun setUserData(navigationView: NavigationView) {
        val tvUsrName =
            navigationView.getHeaderView(0).findViewById<TextView>(R.id.tvUserName)
        val tvEmail =
            navigationView.getHeaderView(0).findViewById<TextView>(R.id.tvUserEmail)
        tvUsrName.text = SavedPreference.getInstance(applicationContext)!!.name
        tvEmail.text = SavedPreference.getInstance(applicationContext)!!.fetchEmailId()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController!!,
            mAppBarConfiguration!!
        ) || super.onSupportNavigateUp()
    }

    private fun signOut() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().signOut()
            SavedPreference.getInstance(applicationContext)!!.logout()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            if (AGConnectAuth.getInstance().currentUser == null) {
                getSharedPreferences("com.agc.fb", Context.MODE_PRIVATE).edit()
                    .clear().apply()
                getSharedPreferences(
                    "AuthStatePreference",
                    Context.MODE_PRIVATE
                ).edit().clear().apply()
            }
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.user_not_logged_in),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareLink() {
        if (agcLink != null) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, agcLink)
            startActivity(intent)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (resultCode == Activity.RESULT_OK) {
                val purchaseResult =
                    getPurchaseResult(this@MainActivity, data)
                if (purchaseResult == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    Toast.makeText(this@MainActivity, R.string.pay_success, Toast.LENGTH_SHORT)
                        .show()
                    upsertData()
                    return
                }
                if (purchaseResult == OrderStatusCode.ORDER_STATE_CANCEL) {
                    Toast.makeText(this@MainActivity, R.string.cancel, Toast.LENGTH_SHORT).show()
                    return
                }
                Toast.makeText(this@MainActivity, R.string.pay_fail, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, R.string.cancel, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Initialize DB
     *
     * @param dbOpenInterface interface for fetching DB result
     */
    fun initializeDb(dbOpenInterface: DBOpenInterface?) {
        if (CloudDb.instance?.cloudDBZone != null) {
            dbOpenInterface?.success(CloudDb.instance!!.cloudDBZone)
        } else {
            CloudDb.instance?.createObjectType()
            CloudDb.instance?.openCloudDBZone(
                object : DBOpenInterface {
                    override fun success(cloudDBZone: CloudDBZone?) {
                        dbOpenInterface?.success(cloudDBZone)
                    }

                    override fun failure() {}
                })
        }
    }

    private fun upsertData() {
        SavedPreference.getInstance(applicationContext)!!.isSubscribed(true)
        initializeDb(object : DBOpenInterface {
            override fun success(cloudDBZone: CloudDBZone?) {
                stopProgressBar()
                mCloudDBZone = cloudDBZone
                checkIfUserExists()
            }

            override fun failure() {
                stopProgressBar()
            }
        })
    }

    /**
     * Checks if user data exists in DB or not
     *
     */
    private fun checkIfUserExists() {
        if (mCloudDBZone == null) {
            Log.w("TAG", "CloudDBZone is null, try re-open it")
            return
        }
        val sp = SavedPreference.getInstance(applicationContext)
        val queryTask: Task<CloudDBZoneSnapshot<Profile>> =
            CloudDbQueries.instance!!.getUserProfile(sp!!, mCloudDBZone!!)
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Profile> ->
                val profile =
                    processProfileQueryResult(snapshot)
                profile!!.subscription = true
                val upsertTask =
                    mCloudDBZone!!.executeUpsert(profile)
                upsertTask
                    .addOnSuccessListener { cloudDBZoneResult: Int? ->
                        Log.d(
                            "TAG",
                            "Profile data added successfully"
                        )
                    }
                    .addOnFailureListener { e: Exception? ->
                        Log.d(
                            "TAG",
                            "addOnFailureListener for profile"
                        )
                    }
                Log.d("TAG", "Fetch user profile data")
            }
            .addOnFailureListener { e: Exception? ->
                Log.d(
                    "TAG",
                    "addOnFailureListener"
                )
            }
    }

    /**
     * Returns user Profile
     *
     * @param snapshot : Cloud DB result
     * @return user Profile
     */
    private fun processProfileQueryResult(snapshot: CloudDBZoneSnapshot<Profile>): Profile? {
        val bookInfoCursor =
            snapshot.snapshotObjects
        var interest: Profile? = null
        try {
            while (bookInfoCursor.hasNext()) {
                interest = bookInfoCursor.next()
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w("TAG", "processQueryResult: AGConnectCloudDBException")
        } finally {
            snapshot.release()
        }
        return interest
    }
}