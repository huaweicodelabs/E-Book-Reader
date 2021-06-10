/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.utility.Constants
import com.huawei.elibri.kotlin.utility.Util.isOnline
import com.huawei.elibri.kotlin.utility.Util.replaceFragment
import com.huawei.elibri.kotlin.view.fragment.LoginFragment

/**
 * Activity for registration
 *
 * @author lWx916345
 * @since 25-10-2020
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        isOnline(applicationContext)
        val bundle = Bundle()
        bundle.putString(
            Constants.TAG,
            Constants.LOGIN_FRAGMENT
        )
        replaceFragment(this@LoginActivity, LoginFragment(), bundle)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}