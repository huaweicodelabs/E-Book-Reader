/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.utility;

/**
 * Constants
 *
 * @author: nWX914751
 * @since: 02-11-2020
 */
public class Constants {
    /**
     * For splash screen delay
     */
    public static final int DELAY_MILLIS = 3000;
    /**
     * Domain uri for app sharing
     */
    public static final String DOMAIN_URI_PREFIX = "https://elibri.dra.agconnect.link";
    /**
     * Deep link for app sharing
     */
    public static final String DEEP_LINK = "https://elibri.com/SplashActivity";
    /**
     * Common tag
     */
    public static final String TAG = "Tag";
    /**
     * Profile pragment tag
     */
    public static final String PROFILE_FRAGMENT = "ProfileFragment";
    /**
     * Login pragment tag
     */
    public static final String LOGIN_FRAGMENT = "LoginFragment";
    /**
     * For shared preference user email
     */
    public static final String EMAIL_ID_KEY = "userEmailK";
    /**
     * Name of shared preference
     */
    public static final String PREFS_NAME = "elibri_prefs";
    /**
     * For shared preference user name
     */
    public static final String NAME_KEY = "userNameK";
    /**
     * For shared preference subscription status
     */
    public static final String SUBSCRIPTION_KEY = "subscribeKey";
    /**
     * Buy code for subscription products
     */
    public static final int REQ_CODE_BUY = 4002;

    /**
     * requestCode for pull up the login page for isEnvReady interface
     */
    public static final int REQ_CODE_LOGIN = 2001;
}
