/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java;

import android.content.Context;
import android.content.SharedPreferences;

import com.huawei.elibri.java.utility.Constants;

/**
 * @author: nWX914751
 * @since: 05-11-2020
 */
public class SavedPreference {
    private static SavedPreference sp;
    private static SharedPreferences prefs;

    private SavedPreference() {}

    /**
     * Save the instance for singleton class
     *
     * @param context of the class which called this method
     * @return the instance of shared preference
     */
    public static SavedPreference getInstance(Context context) {
        if (sp == null) {
            sp = new SavedPreference();
            prefs = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        }
        return sp;
    }

    /**
     * Save email address of the user
     *
     * @param value of email id of user
     */
    public void saveEmailId(String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.EMAIL_ID_KEY, value);
        editor.apply();
    }

    /**
     * To get the email id of current logged in user
     *
     * @return the emaild of the logged in user
     */
    public String fetchEmailId() {
        return prefs.getString(Constants.EMAIL_ID_KEY, "");
    }

    /**
     * Save user membership plan if he purchased it
     *
     * @param value for membership plan as true or false
     */
    public void isSubscribed(boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.SUBSCRIPTION_KEY, value);
        editor.apply();
    }

    /**
     * Check user purchased any membership plan or not
     *
     * @return true or false if user has took the membership plan
     */
    public boolean getIsSubscribed() {
        return prefs.getBoolean(Constants.SUBSCRIPTION_KEY, false);
    }

    /**
     * Save the name of logged in user into shared preference
     *
     * @param name of the logged in user
     */
    public void setName(String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.NAME_KEY, name);
        editor.apply();
    }

    /**
     * Get the name of logged in user
     *
     * @return the name of the user
     */
    public String getName() {
        return prefs.getString(Constants.NAME_KEY, "");
    }

    /**
     * Clear all the data from sharedpreference
     */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
