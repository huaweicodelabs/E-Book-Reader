/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Copyright 2014 Google
 */
package com.huawei.elibri.kotlin.utility

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.huawei.elibri.R
import java.io.IOException

/**
 * Util class to store utility functions of the app
 *
 * @author: nWX914751
 * @since: 27-10-2020
 */
object Util {
    private var progress: ProgressDialog? = null

    /**
     * Loads image from web
     *
     * @param fragment  Fragment where we need to load the image
     * @param url       Image web url
     * @param imageView ImageView in which image needs to be loaded
     */
    fun loadImage(
        fragment: Context?,
        url: String?,
        imageView: ImageView?
    ) {
        Glide.with(fragment!!).load(url).placeholder(R.drawable.ic_baseline_menu_book_gray_color)
            .into(imageView!!)
    }

    /**
     * Replaces the current fragment
     *
     * @param fragmentActivity The fragmentActivity which contains the container
     * @param fragment         Fragment that needs to be added in the container
     * @param bundle           Bundle of arguments
     */
    fun replaceFragment(
        fragmentActivity: FragmentActivity,
        fragment: Fragment?,
        bundle: Bundle
    ) {
        val transaction =
            fragmentActivity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment!!)
        transaction.addToBackStack(bundle.getString(Constants.TAG))
        transaction.commit()
    }

    /**
     * Show the progress bar
     *
     * @param context of the activity or fragment
     */
    fun setProgressBar(context: Context?) {
        progress = ProgressDialog(context)
        progress!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progress!!.isIndeterminate = true
        progress!!.show()
    }

    /**
     * Dismiss progress bar
     */
    fun stopProgressBar() {
        progress!!.hide()
    }

    /**
     * Get the phone storage path
     *
     * @param activity reference
     * @return storage path
     * @throws IOException exception while getting the path
     */
    @Throws(IOException::class)
    fun getSdkDirPath(activity: Context): String? {
        var str: String? = null
        str = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file =
                ContextCompat.getExternalFilesDirs(activity.applicationContext, null)[0]
            file.canonicalPath
        } else {
            activity.filesDir.canonicalPath
        }
        return str
    }

    /**
     * Check internet connection is available or not
     *
     * @param applicationContext which call this method
     * @return true or false
     */
    fun isOnline(applicationContext: Context): Boolean {
        val conMgr = applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMgr.activeNetworkInfo
        if (netInfo == null || !netInfo.isConnected || !netInfo.isAvailable) {
            Toast.makeText(
                applicationContext, applicationContext.getString(R.string.no_internet),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }
}