/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.interfaces

import com.huawei.agconnect.cloud.database.CloudDBZone

/**
 * Interface to get DB open result
 *
 * @author: nWX914751
 * @since: 10-11-2020
 */
interface DBOpenInterface {
    /**
     * To get the success result from cloub db zone
     *
     * @param cloudDBZone object reference
     */
    fun success(cloudDBZone: CloudDBZone?)

    /**
     * To get the failure result from cloub db zone
     */
    fun failure()
}