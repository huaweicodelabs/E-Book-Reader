/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.utility

/**
 * Constants
 *
 * @author: nWX914751
 * @since: 02-11-2020
 */
object Constants {
    /**
     * For splash screen delay
     */
    const val DELAY_MILLIS = 3000

    /**
     * Domain uri for app sharing
     */
    const val DOMAIN_URI_PREFIX = "https://elibri.dra.agconnect.link"

    /**
     * Deep link for app sharing
     */
    const val DEEP_LINK = "https://elibri.com/SplashActivity"

    /**
     * Common tag
     */
    const val TAG = "Tag"

    /**
     * Profile pragment tag
     */
    const val PROFILE_FRAGMENT = "ProfileFragment"

    /**
     * Login pragment tag
     */
    const val LOGIN_FRAGMENT = "LoginFragment"

    /**
     * For shared preference user email
     */
    const val EMAIL_ID_KEY = "userEmailK"

    /**
     * Name of shared preference
     */
    const val PREFS_NAME = "elibri_prefs"

    /**
     * For shared preference user name
     */
    const val NAME_KEY = "userNameK"

    /**
     * For shared preference subscription status
     */
    const val SUBSCRIPTION_KEY = "subscribeKey"

    /**
     * Buy code for subscription products
     */
    const val REQ_CODE_BUY = 4002

    /**
     * requestCode for pull up the login page for isEnvReady interface
     */
    const val REQ_CODE_LOGIN = 2001
}