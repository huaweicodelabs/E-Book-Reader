/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin

import android.content.Context
import android.content.SharedPreferences
import com.huawei.elibri.kotlin.utility.Constants

/**
 * @author: nWX914751
 * @since: 05-11-2020
 */
class SavedPreference private constructor() {
    /**
     * Save email address of the user
     *
     * @param value of email id of user
     */
    fun saveEmailId(value: String?) {
        val editor: SharedPreferences.Editor = prefs!!.edit()
        editor.putString(Constants.EMAIL_ID_KEY, value)
        editor.apply()
    }

    /**
     * To get the email id of current logged in user
     *
     * @return the emaild of the logged in user
     */
    fun fetchEmailId(): String? {
        return prefs!!.getString(
            Constants.EMAIL_ID_KEY,
            ""
        )
    }

    /**
     * Save user membership plan if he purchased it
     *
     * @param value for membership plan as true or false
     */
    fun isSubscribed(value: Boolean) {
        val editor: SharedPreferences.Editor = prefs!!.edit()
        editor.putBoolean(Constants.SUBSCRIPTION_KEY, value)
        editor.apply()
    }

    /**
     * Check user purchased any membership plan or not
     *
     * @return true or false if user has took the membership plan
     */
    val isSubscribed: Boolean
        get() = prefs!!.getBoolean(
            Constants.SUBSCRIPTION_KEY,
            false
        )

    /**
     * Get the name of logged in user
     *
     * @return the name of the user
     */
    /**
     * Save the name of logged in user into shared preference
     *
     * @param name of the logged in user
     */
    var name: String?
        get() {
            return prefs!!.getString(
                Constants.NAME_KEY,
                ""
            )
        }
        set(name) {
            val editor: SharedPreferences.Editor = prefs!!.edit()
            editor.putString(Constants.NAME_KEY, name)
            editor.apply()
        }

    /**
     * Clear all the data from sharedpreference
     */
    fun logout() {
        val editor: SharedPreferences.Editor = prefs!!.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private var sp: SavedPreference? = null
        private var prefs: SharedPreferences? = null

        /**
         * Save the instance for singleton class
         *
         * @param context of the class which called this method
         * @return the instance of shared preference
         */
        fun getInstance(context: Context): SavedPreference? {
            if (sp == null) {
                sp = SavedPreference()
                prefs =
                    context.getSharedPreferences(Constants.PREFS_NAME, 0)
            }
            return sp
        }
    }
}