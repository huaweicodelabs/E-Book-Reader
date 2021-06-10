/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.iap;

import android.app.Activity;
import android.content.IntentSender;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.StartIapActivityReq;
import com.huawei.hms.iap.entity.StartIapActivityResult;
import com.huawei.hms.support.api.client.Status;

import java.util.List;

/**
 * @author: lWX916345
 * @since: 02-11-2020
 */
public class IapRequestHelper {
    private static final String TAG = IapRequestHelper.class.getName();

    /**
     * Create a PurchaseIntentReq object.
     *
     * @param type In-app product type.The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productId ID of the in-app product to be paid.
     * The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private static PurchaseIntentReq createPurchaseIntentReq(int type, String productId) {
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setPriceType(type);
        req.setProductId(productId);
        req.setDeveloperPayload("testPurchase");
        return req;
    }


    /**
     * Create a OwnedPurchasesReq object.
     *
     * @param type type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param continuationToken A data location flag which returns from obtainOwnedPurchases
     * api or obtainOwnedPurchaseRecord api.
     * @return OwnedPurchasesReq
     */
    private static OwnedPurchasesReq createOwnedPurchasesReq(int type, String continuationToken) {
        OwnedPurchasesReq req = new OwnedPurchasesReq();
        req.setPriceType(type);
        req.setContinuationToken(continuationToken);
        return req;
    }

    /**
     * Create a ProductInfoReq object.
     *
     * @param type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @return ProductInfoReq
     */
    private static ProductInfoReq createProductInfoReq(int type, List<String> productIds) {
        ProductInfoReq req = new ProductInfoReq();
        req.setPriceType(type);
        req.setProductIds(productIds);
        return req;
    }

    /**
     * To check whether the country or region of the logged in HUAWEI ID is included
     * in the countries or regions supported by HUAWEI IAP.
     *
     * @param mClient IapClient instance to call the isEnvReady API.
     * @param callback IapApiCallback.
     */
    public static void isEnvReady(IapClient mClient, final IapApiCallback callback) {
        Task<IsEnvReadyResult> task = mClient.isEnvReady();
        task.addOnSuccessListener(
                        new OnSuccessListener<IsEnvReadyResult>() {
                            @Override
                            public void onSuccess(IsEnvReadyResult result) {
                                callback.onSuccess(result);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "isEnvReady, fail");
                                callback.onFail(e);
                            }
                        });
    }

    /**
     * Obtain in-app product details configured in AppGallery Connect.
     *
     * @param iapClient IapClient instance to call the obtainProductInfo API.
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param callback IapApiCallback
     */
    public static void obtainProductInfo(
            IapClient iapClient, final List<String> productIds, int type, final IapApiCallback callback) {
        Task<ProductInfoResult> task = iapClient.obtainProductInfo(createProductInfoReq(type, productIds));
        task.addOnSuccessListener(
                        new OnSuccessListener<ProductInfoResult>() {
                            @Override
                            public void onSuccess(ProductInfoResult result) {
                                callback.onSuccess(result);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "obtainProductInfo, fail");
                                callback.onFail(e);
                            }
                        });
    }

    /**
     * create orders for in-app products in the PMS
     *
     * @param iapClient IapClient instance to call the createPurchaseIntent API.
     * @param productId ID of the in-app product to be paid.
     * @param type  In-app product type.
     *              The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param callback IapApiCallback
     */
    public static void createPurchaseIntent(
            final IapClient iapClient, String productId, int type, final IapApiCallback callback) {
        Task<PurchaseIntentResult> task = iapClient.createPurchaseIntent(createPurchaseIntentReq(type, productId));
        task.addOnSuccessListener(
                        new OnSuccessListener<PurchaseIntentResult>() {
                            @Override
                            public void onSuccess(PurchaseIntentResult result) {
                                callback.onSuccess(result);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "createPurchaseIntent, fail");
                                callback.onFail(e);
                            }
                        });
    }

    /**
     * to start an activity.
     *
     * @param activity the activity to launch a new page.
     * @param status This parameter contains the pendingIntent object of the payment page.
     * @param reqCode Result code.
     */
    public static void startResolutionForResult(Activity activity, Status status, int reqCode) {
        if (status == null) {
            Log.e(TAG, "status is null");
            return;
        }
        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(activity, reqCode);
            } catch (IntentSender.SendIntentException exp) {
                Log.e(TAG, "SendIntentException");
            }
        } else {
            Log.e(TAG, "intent is null");
        }
    }

    /**
     * query information about all subscribed in-app products, including consumables, non-consumables,
     * and auto-renewable subscriptions.
     * If consumables are returned, the system needs to deliver them and calls the
     * consumeOwnedPurchase API to consume the products.
     * If non-consumables are returned, the in-app products do not need to be consumed.
     * If subscriptions are returned, all existing subscription relationships of the
     * user under the app are returned.
     *
     * @param mClient IapClient instance to call the obtainOwnedPurchases API.
     * @param type In-app product type.The value contains: 0:
     * consumable 1: non-consumable 2 auto-renewable subscription
     * @param continuationToken is the token
     * @param callback IapApiCallback
     */
    public static void obtainOwnedPurchases(
            IapClient mClient, final int type, String continuationToken, final IapApiCallback callback) {
        Task<OwnedPurchasesResult> task =
                mClient.obtainOwnedPurchases(IapRequestHelper.createOwnedPurchasesReq(type, continuationToken));
        task.addOnSuccessListener(
                        new OnSuccessListener<OwnedPurchasesResult>() {
                            @Override
                            public void onSuccess(OwnedPurchasesResult result) {
                                callback.onSuccess(result);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "obtainOwnedPurchases, fail");
                                callback.onFail(e);
                            }
                        });
    }

    /**
     * link to subscription manager page
     *
     * @param activity activity
     * @param product_id the productId of the subscription product
     */
    public static void showSubscription(final Activity activity, String product_id) {
        StartIapActivityReq req = new StartIapActivityReq();
        if (TextUtils.isEmpty(product_id)) {
            req.setType(StartIapActivityReq.TYPE_SUBSCRIBE_MANAGER_ACTIVITY);
        } else {
            req.setType(StartIapActivityReq.TYPE_SUBSCRIBE_EDIT_ACTIVITY);
            req.setSubscribeProductId(product_id);
        }

        IapClient iapClient = Iap.getIapClient(activity);
        Task<StartIapActivityResult> task = iapClient.startIapActivity(req);

        task.addOnSuccessListener(
                        new OnSuccessListener<StartIapActivityResult>() {
                            @Override
                            public void onSuccess(StartIapActivityResult result) {
                                if (result != null) {
                                    result.startActivity(activity);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                ExceptionHandle.handle(activity, e);
                            }
                        });
    }
}
