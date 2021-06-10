/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.utility;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.huawei.elibri.R;
import com.huawei.elibri.java.interfaces.DialogClickInterface;

/**
 * Class foe dialogs
 *
 * @author: nWX914751
 * @since: 09-11-2020
 */
public class DialogUtils {
    /**
     * to show the alert dialog
     *
     * @param context         : Activity context
     * @param message         : Message of the dialog
     * @param dialogInterface : Interface to identify clicks
     */
    public static void showPositiveAlertDialog(Context context, String message, DialogClickInterface dialogInterface) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.alert))
                .setMessage(message)
                .setPositiveButton(
                        android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            /**
                             * On click of positive of dialog
                             *
                             * @param dialog The dialog that received the click.
                             * @param which The button that was clicked
                             */
                            public void onClick(DialogInterface dialog, int which) {
                                dialogInterface.positiveButton();
                            }
                        })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
