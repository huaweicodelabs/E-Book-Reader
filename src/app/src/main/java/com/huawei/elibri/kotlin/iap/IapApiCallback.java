/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.kotlin.iap;

/** Interface for subscription
 *
 * @author: lWX916345
 * @since: 02-11-2020
 */
public interface IapApiCallback<T> {
    /**
     * The request is successful.
     *
     * @param result The result of a successful response.
     */
    void onSuccess(T result);

    /**
     * Callback fail.
     *
     * @param e An Exception from IAPSDK.
     */
    void onFail(Exception e);
}
