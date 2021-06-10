/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.service.repository;

import android.util.Log;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.hmf.tasks.Task;

/**
 * This class interacts with the Huawei cloud
 *
 * @author: nWX914751
 * @since: 30-10-2020
 */
public class CloudDb {
    private static final String TAG = CloudDb.class.getSimpleName();
    private static AGConnectCloudDB sAgconnectCloudDb;
    private static CloudDb sCloudDb;
    private CloudDBZone mCloudDBZone;

    /**
     * Get the cloud DB zone object
     *
     * @return cloudDb zone
     */
    public CloudDBZone getCloudDBZone() {
        return mCloudDBZone;
    }

    /**
     * Cloud Db constructor
     */
    private CloudDb() {}

    /**
     * Initializes Cloud Db
     *
     * @return Cloud DB instance
     */
    public static synchronized CloudDb getInstance() {
        if (sCloudDb == null) {
            sCloudDb = new CloudDb();
            sAgconnectCloudDb = AGConnectCloudDB.getInstance();
        }
        return sCloudDb;
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            if (sAgconnectCloudDb != null)
            sAgconnectCloudDb.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException exception) {
            Log.e(TAG, "AGConnectCloudDBException");
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     *
     * @param dialogInterface to get the callback from Cloud db zone
     */
    public void openCloudDBZone(DBOpenInterface dialogInterface) {
        CloudDBZoneConfig mConfig =
                new CloudDBZoneConfig(
                        CloudDbConstant.DB_NAME, CloudDbConstant.SYNC_PROPERTY, CloudDbConstant.ACCESS_ZONE);
        mConfig.setPersistenceEnabled(true);
        if (sAgconnectCloudDb != null) {
            Task<CloudDBZone> openDBZoneTask = sAgconnectCloudDb.openCloudDBZone2(mConfig, true);
            openDBZoneTask
                    .addOnSuccessListener(
                            cloudDbZone -> {
                                mCloudDBZone = cloudDbZone;
                                dialogInterface.success(cloudDbZone);
                            })
                    .addOnFailureListener(
                            exception -> {
                                dialogInterface.failure();
                            });
        }
    }
}
