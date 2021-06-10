/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin

import android.app.Application
import android.content.Context
import com.huawei.agconnect.cloud.database.AGConnectCloudDB

/**
 * Application class
 *
 * @author: nWX914751
 * @since: 30-10-2020
 */
class EBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAGConnectCloudDB(this)
    }

    companion object {
        /**
         * Init AGConnectCloudDB in Application
         *
         * @param context application context
         */
        fun initAGConnectCloudDB(context: Context?) {
            AGConnectCloudDB.initialize(context!!)
        }
    }
}