/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.elibri.R;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.service.repository.CloudDb;

/**
 * Base Fragment
 *
 * @author: nWX914751
 * @since: 26-11-2020
 */
class BaseFragment extends Fragment {
    /**
     * Global toast for complete application
     */
    protected Toast toast;

    /**
     * Shows alert dialog
     *
     * @param content Context of screen
     */
    protected void showDialog(Context content) {
        AlertDialog.Builder ab = new AlertDialog.Builder(content);
        ab.setTitle(getString(R.string.app_name));
        ab.setMessage(R.string.exit_app);
        ab.setPositiveButton(
                R.string.yes,
                (dialog, which) -> {
                    dialog.dismiss();
                    // if you want to kill app . from other then your main avtivity.(Launcher)
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                });
        ab.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        ab.show();
    }

    /**
     * Initialize DB
     *
     * @param dbOpenInterface interface for fetching DB result
     */
    public void initializeDb(DBOpenInterface dbOpenInterface) {
        if (null != CloudDb.getInstance().getCloudDBZone()) {
            if (dbOpenInterface != null) {
                dbOpenInterface.success(CloudDb.getInstance().getCloudDBZone());
            }
        } else {
            CloudDb.getInstance().createObjectType();
            CloudDb.getInstance()
                    .openCloudDBZone(
                            new DBOpenInterface() {
                                @Override
                                public void success(CloudDBZone cloudDBZone) {
                                    if (dbOpenInterface != null) {
                                        dbOpenInterface.success(cloudDBZone);
                                    }
                                }

                                @Override
                                public void failure() {
                                }
                            });
        }
    }

    /**
     * Display toast
     *
     * @param message that we want to display in toast
     * @param context of the class
     */
    public void showToast(String message, Context context) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    void showAlertExitDialog(View view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(
                        (v, i, keyEvent) -> {
                            if (keyEvent.getAction() == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                                showDialog(getContext());
                                return true;
                            }
                            return false;
                        });
    }
}
