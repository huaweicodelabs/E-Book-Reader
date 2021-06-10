/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.service.repository

import android.util.Log
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface

/**
 * This class interacts with the Huawei cloud
 *
 * @author: nWX914751
 * @since: 30-10-2020
 */
class CloudDb
/**
 * Cloud Db constructor
 */
private constructor() {
    /**
     * Get the cloud DB zone object
     *
     * @return cloudDb zone
     */
    var cloudDBZone: CloudDBZone? = null
        private set

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    fun createObjectType() {
        try {
            if (sAgconnectCloudDb != null) sAgconnectCloudDb!!.createObjectType(
                ObjectTypeInfoHelper.getObjectTypeInfo()
            )
        } catch (exception: AGConnectCloudDBException) {
            Log.e(TAG, "AGConnectCloudDBException")
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     *
     * @param dialogInterface to get the callback from Cloud db zone
     */
    fun openCloudDBZone(dialogInterface: DBOpenInterface) {
        val mConfig = CloudDBZoneConfig(
            CloudDbConstant.DB_NAME, CloudDbConstant.SYNC_PROPERTY, CloudDbConstant.ACCESS_ZONE
        )
        mConfig.persistenceEnabled = true
        if (sAgconnectCloudDb != null) {
            val openDBZoneTask =
                sAgconnectCloudDb!!.openCloudDBZone2(mConfig, true)
            openDBZoneTask
                .addOnSuccessListener { cloudDbZone: CloudDBZone? ->
                    cloudDBZone = cloudDbZone
                    dialogInterface.success(cloudDbZone)
                }
                .addOnFailureListener { exception: Exception? -> dialogInterface.failure() }
        }
    }

    companion object {
        private val TAG = CloudDb::class.java.simpleName
        private var sAgconnectCloudDb: AGConnectCloudDB? = null
        private var sCloudDb: CloudDb? = null

        /**
         * Initializes Cloud Db
         *
         * @return Cloud DB instance
         */
        @get:Synchronized
        val instance: CloudDb?
            get() {
                if (sCloudDb == null) {
                    sCloudDb = CloudDb()
                    sAgconnectCloudDb = AGConnectCloudDB.getInstance()
                }
                return sCloudDb
            }
    }
}