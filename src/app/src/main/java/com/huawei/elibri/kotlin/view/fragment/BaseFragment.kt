/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Process
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.service.repository.CloudDb

/**
 * Base Fragment
 *
 * @author: nWX914751
 * @since: 26-11-2020
 */
open class BaseFragment : Fragment() {
    /**
     * Global toast for complete application
     */
    protected var toast: Toast? = null

    /**
     * Shows alert dialog
     *
     * @param content Context of screen
     */
    protected fun showDialog(content: Context?) {
        val ab =
            AlertDialog.Builder(content!!)
        ab.setTitle(getString(R.string.app_name))
        ab.setMessage(R.string.exit_app)
        ab.setPositiveButton(
            R.string.yes
        ) { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            // if you want to kill app . from other then your main avtivity.(Launcher)
            Process.killProcess(Process.myPid())
            System.exit(1)
        }
        ab.setNegativeButton(
            R.string.no
        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        ab.show()
    }

    /**
     * Initialize DB
     *
     * @param dbOpenInterface interface for fetching DB result
     */
    open fun initializeDb(dbOpenInterface: DBOpenInterface?) {
        if (null != CloudDb.instance?.cloudDBZone) {
            dbOpenInterface?.success(CloudDb.instance?.cloudDBZone)
        } else {
            CloudDb.instance?.createObjectType()
            CloudDb.instance?.openCloudDBZone(
                    object : DBOpenInterface {
                        override fun success(cloudDBZone: CloudDBZone?) {
                            dbOpenInterface?.success(cloudDBZone)
                        }

                        override fun failure() {}
                    })
        }
    }
    /**
     * Display toast
     *
     * @param message that we want to display in toast
     * @param context of the class
     */
    fun showToast(message: String?, context: Context?) {
        if (toast != null) {
            toast!!.cancel()
        }
       val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    open fun showAlertExitDialog(view: View) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { v: View?, i: Int, keyEvent: KeyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                showDialog(context)
                return@setOnKeyListener true
            }
            false
        }
    }
}