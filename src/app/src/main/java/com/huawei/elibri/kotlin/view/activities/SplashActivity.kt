/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.utility.Constants
import com.huawei.elibri.kotlin.utility.Util.isOnline
import com.huawei.hms.mlsdk.common.MLApplication

/**
 * Displays Splash screen
 *
 * @author: nWX914751
 * @since: 30-10-2020
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOnline(applicationContext)

        // Initialize ML kit analyzer
        MLApplication.getInstance().apiKey =
            AGConnectServicesConfig.fromContext(this@SplashActivity)
                .getString("client/app_id")
        AGConnectCrash.getInstance().enableCrashCollection(true)
        Handler()
            .postDelayed(
                {
                    if (!TextUtils.isEmpty(
                            SavedPreference.getInstance(applicationContext)?.fetchEmailId()
                        )
                        || !TextUtils.isEmpty(
                            SavedPreference
                                .getInstance(applicationContext)!!.name
                        )
                    ) {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                },
                Constants.DELAY_MILLIS.toLong()
            )
    }
}