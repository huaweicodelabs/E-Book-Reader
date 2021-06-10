/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Copyright 2014 Google
 */

package com.huawei.elibri.java.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.huawei.elibri.R;

import java.io.File;
import java.io.IOException;

/**
 * Util class to store utility functions of the app
 *
 * @author: nWX914751
 * @since: 27-10-2020
 */
public class Util {
    private static ProgressDialog progress;

    /**
     * Loads image from web
     *
     * @param fragment  Fragment where we need to load the image
     * @param url       Image web url
     * @param imageView ImageView in which image needs to be loaded
     */
    public static void loadImage(Context fragment, String url, ImageView imageView) {
        Glide.with(fragment).load(url).placeholder(R.drawable.ic_baseline_menu_book_gray_color).into(imageView);
    }

    /**
     * Replaces the current fragment
     *
     * @param fragmentActivity The fragmentActivity which contains the container
     * @param fragment         Fragment that needs to be added in the container
     * @param bundle           Bundle of arguments
     */
    public static void replaceFragment(FragmentActivity fragmentActivity, Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(bundle.getString(Constants.TAG));
        transaction.commit();
    }

    /**
     * Show the progress bar
     *
     * @param context of the activity or fragment
     */
    public static void setProgressBar(Context context) {
        progress = new ProgressDialog(context);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.show();
    }

    /**
     * Dismiss progress bar
     */
    public static void stopProgressBar() {
        progress.hide();
    }

    /**
     * Get the phone storage path
     *
     * @param activity reference
     * @return storage path
     * @throws IOException exception while getting the path
     */
    public static String getSdkDirPath(Context activity) throws IOException {
        String str = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = ContextCompat.getExternalFilesDirs(activity.getApplicationContext(), null)[0];
            str = file.getCanonicalPath();
        } else {
            str = activity.getFilesDir().getCanonicalPath();
        }
        return str;
    }

    /**
     * Check internet connection is available or not
     *
     * @param applicationContext which call this method
     * @return true or false
     *
     */
    public static boolean isOnline(Context applicationContext) {
        ConnectivityManager conMgr = (ConnectivityManager) applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(applicationContext, applicationContext.getString(R.string.no_internet),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
