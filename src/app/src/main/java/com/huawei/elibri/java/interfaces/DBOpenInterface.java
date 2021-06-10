/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.interfaces;

import com.huawei.agconnect.cloud.database.CloudDBZone;

/**
 * Interface to get DB open result
 *
 * @author: nWX914751
 * @since: 10-11-2020
 */
public interface DBOpenInterface {
    /**
     * To get the success result from cloub db zone
     *
     * @param cloudDBZone object reference
     */
    void success(CloudDBZone cloudDBZone);

    /**
     * To get the failure result from cloub db zone
     */
    void failure();
}
