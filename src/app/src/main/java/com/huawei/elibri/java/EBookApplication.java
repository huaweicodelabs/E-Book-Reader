/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java;

import android.app.Application;
import android.content.Context;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;

/**
 * Application class
 *
 * @author: nWX914751
 * @since: 30-10-2020
 */
public class EBookApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initAGConnectCloudDB(this);
    }

    /**
     * Init AGConnectCloudDB in Application
     *
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }
}
