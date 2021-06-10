/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.utility

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.interfaces.DialogClickInterface

/**
 * Class foe dialogs
 *
 * @author: nWX914751
 * @since: 09-11-2020
 */
object DialogUtils {
    /**
     * to show the alert dialog
     *
     * @param context         : Activity context
     * @param message         : Message of the dialog
     * @param dialogInterface : Interface to identify clicks
     */
    fun showPositiveAlertDialog(
        context: Context,
        message: String?,
        dialogInterface: DialogClickInterface
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.alert))
            .setMessage(message)
            .setPositiveButton(
                android.R.string.yes
            ) { dialog, which ->
                /**
                 * On click of positive of dialog
                 *
                 * @param dialog The dialog that received the click.
                 * @param which The button that was clicked
                 */
                dialogInterface.positiveButton()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}