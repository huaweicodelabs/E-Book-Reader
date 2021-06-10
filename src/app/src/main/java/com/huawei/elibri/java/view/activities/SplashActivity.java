/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.utility.Constants;
import com.huawei.hms.mlsdk.common.MLApplication;

import static com.huawei.elibri.java.utility.Util.isOnline;

/**
 * Displays Splash screen
 *
 * @author: nWX914751
 * @since: 30-10-2020
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isOnline(getApplicationContext());

        // Initialize ML kit analyzer
        MLApplication.getInstance()
                .setApiKey(AGConnectServicesConfig.fromContext(SplashActivity.this)
                        .getString("client/app_id"));

        AGConnectCrash.getInstance().enableCrashCollection(true);
        new Handler()
                .postDelayed(
                        () -> {
                            if (!TextUtils.isEmpty(SavedPreference.getInstance(getApplicationContext())
                                    .fetchEmailId())
                                    || !TextUtils.isEmpty(SavedPreference
                                    .getInstance(getApplicationContext()).getName())) {
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        Constants.DELAY_MILLIS);
    }
}
