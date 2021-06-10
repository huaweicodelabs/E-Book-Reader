/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.iap;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;

import org.json.JSONException;

import java.util.List;

/**
 * Util for Subscription function.
 *
 * @author lWX916345
 * @since 12-11-2020
 */
public class SubscriptionUtils {
    private static final String TAG = "SubscriptionUtils";

    /**
     * Decide whether to offer subscription service
     *
     * @param result the OwnedPurchasesResult from IapClient.obtainOwnedPurchases
     * @param productId subscription product id
     * @return decision result
     */
    public static boolean shouldOfferService(OwnedPurchasesResult result, String productId) {
        if (result == null) {
            Log.e(TAG, "OwnedPurchasesResult is null");
            return false;
        }

        List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
        for (String data : inAppPurchaseDataList) {
            try {
                InAppPurchaseData inAppPurchaseData = new InAppPurchaseData(data);
                if (productId.equals(inAppPurchaseData.getProductId())) {
                    int index = inAppPurchaseDataList.indexOf(data);
                    String signature = result.getInAppSignature().get(index);
                    boolean credible = CipherUtil.doCheck(data, signature, CipherUtil.getPublicKey());

                    if (credible) {
                        return inAppPurchaseData.isSubValid();
                    } else {
                        Log.e(TAG, "check the data signature fail");
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "parse InAppPurchaseData JSONException");
                return false;
            }
        }
        return false;
    }

    /**
     * Parse PurchaseResult data from intent
     *
     * @param activity Activity
     * @param data the intent from onActivityResult
     * @return result status
     */
    public static int getPurchaseResult(Activity activity, Intent data) {
        PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(data);
        if (purchaseResultInfo == null) {
            Log.e(TAG, "PurchaseResultInfo is null");
            return OrderStatusCode.ORDER_STATE_FAILED;
        }

        int returnCode = purchaseResultInfo.getReturnCode();
        String errMsg = purchaseResultInfo.getErrMsg();
        switch (returnCode) {
            case OrderStatusCode.ORDER_PRODUCT_OWNED:
                return OrderStatusCode.ORDER_PRODUCT_OWNED;

            case OrderStatusCode.ORDER_STATE_SUCCESS:
                boolean credible =
                        CipherUtil.doCheck(
                                purchaseResultInfo.getInAppPurchaseData(),
                                purchaseResultInfo.getInAppDataSignature(),
                                CipherUtil.getPublicKey());
                if (credible) {
                    try {
                        InAppPurchaseData inAppPurchaseData =
                                new InAppPurchaseData(purchaseResultInfo.getInAppPurchaseData());
                        if (inAppPurchaseData.isSubValid()) {
                            return OrderStatusCode.ORDER_STATE_SUCCESS;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "parse InAppPurchaseData JSONException");
                        return OrderStatusCode.ORDER_STATE_FAILED;
                    }
                } else {
                    Log.e(TAG, "check the data signature fail");
                }
                return OrderStatusCode.ORDER_STATE_FAILED;

            default:
                Log.e(TAG, "returnCode: " + returnCode + " , errMsg: " + errMsg);
                return returnCode;
        }
    }
}
