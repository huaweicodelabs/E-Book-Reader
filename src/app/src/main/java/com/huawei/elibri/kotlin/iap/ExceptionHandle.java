/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.kotlin.iap;

import android.app.Activity;
import android.widget.Toast;
import com.huawei.elibri.kotlin.utility.Constants;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.OrderStatusCode;

/** IAP exceptions
 *
 * @author: lWX916345
 * @since: 02-11-2020
 */
public class ExceptionHandle {
    /**
     * The exception is solved.
     */
    public static final int SOLVED = 0;

    /**
     * Handles the exception returned from the iap api.
     *
     * @param activity The Activity to call the iap api.
     * @param exception The exception returned from the iap api.
     * @return int
     */
    public static int handle(Activity activity, Exception exception) {
        if (exception instanceof IapApiException) {
            IapApiException iapApiException = (IapApiException) exception;
            switch (iapApiException.getStatusCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(activity, "Order has been canceled!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_STATE_PARAM_ERROR:
                    Toast.makeText(activity, "Order state param error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_STATE_NET_ERROR:
                    Toast.makeText(activity, "Order state net error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_VR_UNINSTALL_ERROR:
                    Toast.makeText(activity, "Order vr uninstall error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_HWID_NOT_LOGIN:
                    IapRequestHelper.startResolutionForResult(
                            activity, iapApiException.getStatus(), Constants.REQ_CODE_LOGIN);
                    return SOLVED;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    Toast.makeText(activity, "Product already owned error!", Toast.LENGTH_SHORT).show();
                    return OrderStatusCode.ORDER_PRODUCT_OWNED;
                case OrderStatusCode.ORDER_PRODUCT_NOT_OWNED:
                    Toast.makeText(activity, "Product not owned error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_PRODUCT_CONSUMED:
                    Toast.makeText(activity, "Product consumed error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED:
                    Toast.makeText(activity, "Order account area not supported error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                case OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT:
                    Toast.makeText(activity, "User does not agree the agreement", Toast.LENGTH_SHORT).show();
                    return SOLVED;
                default:
                    Toast.makeText(activity, "Order unknown error!", Toast.LENGTH_SHORT).show();
                    return SOLVED;
            }
        } else {
            Toast.makeText(activity, "external error", Toast.LENGTH_SHORT).show();
            return SOLVED;
        }
    }
}
