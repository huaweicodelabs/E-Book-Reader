/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.iap;

import android.text.TextUtils;
import android.widget.Toast;
import com.huawei.elibri.java.utility.Constants;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import java.util.List;

/**
 * Presenter for Subscription `function.
 *
 * @author lWX916345
 * @since 12-11-2020
 */
public class SubscriptionPresenter implements SubscriptionContract.Presenter {
    private static final String TAG = SubscriptionPresenter.class.getName();
    private SubscriptionContract.View view;
    private OwnedPurchasesResult cacheOwnedPurchasesResult;

    /**
     * Init SubscriptionPresenter
     *
     * @param view the view which the data place in
     */
    public SubscriptionPresenter(SubscriptionContract.View view) {
        setView(view);
    }

    @Override
    public void setView(SubscriptionContract.View view) {
        if (view == null) {
            throw new NullPointerException("can not set null view");
        }
        this.view = view;
    }

    @Override
    public void load(List<String> productids) {
        queryProducts(productids);
        refreshSubscription();
    }

    @Override
    public void refreshSubscription() {
        querySubscriptions(
                new SubscriptionContract.ResultCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean status) {
                        view.updateProductStatus(cacheOwnedPurchasesResult);
                    }
                },
                null);
    }

    private void queryProducts(List<String> productIds) {
        IapRequestHelper.obtainProductInfo(
                Iap.getIapClient(view.getActivity()),
                productIds,
                IapClient.PriceType.IN_APP_SUBSCRIPTION,
                new IapApiCallback<ProductInfoResult>() {
                    @Override
                    public void onSuccess(final ProductInfoResult result) {
                        if (result == null) {
                            return;
                        }
                        List<ProductInfo> productInfos = result.getProductInfoList();
                        view.showProducts(productInfos);
                    }

                    @Override
                    public void onFail(Exception exception) {
                        int error = ExceptionHandle.handle(view.getActivity(), exception);
                        if (error != ExceptionHandle.SOLVED) {
                            Toast.makeText(view.getActivity(), "error code " + String.valueOf(error),
                                    Toast.LENGTH_SHORT).show();
                        }
                        view.showProducts(null);
                    }
                });
    }

    private void querySubscriptions(
            final SubscriptionContract.ResultCallback<Boolean> callback, String continuationToken) {
        IapRequestHelper.obtainOwnedPurchases(
                Iap.getIapClient(view.getActivity()),
                IapClient.PriceType.IN_APP_SUBSCRIPTION,
                continuationToken,
                new IapApiCallback<OwnedPurchasesResult>() {
                    @Override
                    public void onSuccess(OwnedPurchasesResult result) {
                        cacheOwnedPurchasesResult = result;
                        callback.onResult(true);
                    }

                    @Override
                    public void onFail(Exception exception) {
                        ExceptionHandle.handle(view.getActivity(), exception);
                        callback.onResult(false);
                    }
                });
    }

    @Override
    public void buy(final String productId) {
        cacheOwnedPurchasesResult = null;
        IapClient iapClient = Iap.getIapClient(view.getActivity());
        IapRequestHelper.createPurchaseIntent(
                iapClient,
                productId,
                IapClient.PriceType.IN_APP_SUBSCRIPTION,
                new IapApiCallback<PurchaseIntentResult>() {
                    @Override
                    public void onSuccess(PurchaseIntentResult result) {
                        if (result == null) {
                            return;
                        }
                        // you should pull up the page to complete the payment process
                        IapRequestHelper.startResolutionForResult(
                                view.getActivity(), result.getStatus(), Constants.REQ_CODE_BUY);
                    }

                    @Override
                    public void onFail(Exception exception) {
                        int errorCode = ExceptionHandle.handle(view.getActivity(), exception);
                        if ( errorCode != ExceptionHandle.SOLVED) {
                            if (errorCode == OrderStatusCode.ORDER_PRODUCT_OWNED) {
                                showSubscription(productId);
                            }
                        }
                    }
                });
    }

    @Override
    public void showSubscription(String productId) {
        IapRequestHelper.showSubscription(view.getActivity(), productId);
    }

    @Override
    public void shouldOfferService(
            final String productId, final SubscriptionContract.ResultCallback<Boolean> callback) {
        if (callback == null || TextUtils.isEmpty(productId)) {
            return;
        }

        if (cacheOwnedPurchasesResult != null) {
            boolean shouldOffer = SubscriptionUtils.shouldOfferService(cacheOwnedPurchasesResult, productId);
            callback.onResult(shouldOffer);
        } else {
            querySubscriptions(
                    new SubscriptionContract.ResultCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            boolean shouldOffer =
                                    SubscriptionUtils.shouldOfferService(cacheOwnedPurchasesResult, productId);
                            callback.onResult(shouldOffer);
                        }
                    },
                    null);
        }
    }
}
