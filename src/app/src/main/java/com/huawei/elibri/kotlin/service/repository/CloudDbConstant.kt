/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.service.repository

import com.huawei.agconnect.cloud.database.CloudDBZoneConfig.CloudDBZoneAccessProperty
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig.CloudDBZoneSyncProperty
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery.CloudDBZoneQueryPolicy

/**
 * @author: nWX914751
 * @since: 25-11-2020
 */
object CloudDbConstant {
    /**
     * Queries from the local cache.
     */
    val POLICY =
        CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY

    /**
     * Cloud DB name of project
     */
    const val DB_NAME = "ebook"

    /**
     * Cache mode. Data is stored on the cloud, the device is notified only when the data on the cloud changes.
     *
     */
    val SYNC_PROPERTY =
        CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE

    /**
     * Public mode to store the data on cloud
     */
    val ACCESS_ZONE =
        CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
}