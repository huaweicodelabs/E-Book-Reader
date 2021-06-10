/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.service.repository;

import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;

/**
 * @author: nWX914751
 * @since: 25-11-2020
 */
public class CloudDbConstant {
    /**
     * Queries from the local cache.
     */
    public static final CloudDBZoneQuery.CloudDBZoneQueryPolicy POLICY =
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY;
    /**
     * Cloud DB name of project
     */
    public static final String DB_NAME = "ebook";

    /**
     * Cache mode. Data is stored on the cloud, the device is notified only when the data on the cloud changes.
     *
     */
    public static final CloudDBZoneConfig.CloudDBZoneSyncProperty SYNC_PROPERTY =
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE;

    /**
     * Public mode to store the data on cloud
     */
    public static final CloudDBZoneConfig.CloudDBZoneAccessProperty ACCESS_ZONE =
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC;
}
