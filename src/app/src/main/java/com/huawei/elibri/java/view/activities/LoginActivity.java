/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.elibri.R;
import com.huawei.elibri.java.utility.Constants;
import com.huawei.elibri.java.utility.Util;
import com.huawei.elibri.java.view.fragment.LoginFragment;

import static com.huawei.elibri.java.utility.Util.isOnline;

/**
 * Activity for registration
 *
 * @author lWx916345
 * @since 25-10-2020
 */
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        isOnline(getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TAG, Constants.LOGIN_FRAGMENT);
        Util.replaceFragment(LoginActivity.this, new LoginFragment(), bundle);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
