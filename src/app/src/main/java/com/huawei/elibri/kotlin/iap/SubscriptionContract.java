/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.kotlin.iap;

import android.app.Activity;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import java.util.List;

/**
 * @author: lWX916345
 * @since: 02-11-2020
 */
public interface SubscriptionContract {
    interface View {
        /**
         * Show subscription products
         *
         * @param productInfoList Product list
         */
        void showProducts(List<ProductInfo> productInfoList);

        /**
         * Update product purchase status
         *
         * @param ownedPurchasesResult Purchases result
         */
        void updateProductStatus(OwnedPurchasesResult ownedPurchasesResult);

        /**
         * Get Activity
         *
         * @return Activity
         */
        Activity getActivity();
    }

    interface Presenter {
        /**
         * Set the view for presenting data
         *
         * @param view The view for presenting data
         */
        void setView(View view);

        /**
         * Load product data according product Ids
         *
         * @param productIds Product Ids
         */
        void load(List<String> productIds);

        /**
         * Refresh owned subscriptions
         */
        void refreshSubscription();

        /**
         * Buy a subscription product according to productId
         *
         * @param productId Subscription product id
         */
        void buy(String productId);

        /**
         * Show subscription detail
         *
         * @param productId Owned subscription product id
         */
        void showSubscription(String productId);

        /**
         * Decide whether to offer subscription service
         *
         * @param productId Subscription product id
         * @param callback Result callback
         */
        void shouldOfferService(String productId, ResultCallback<Boolean> callback);
    }

    interface ResultCallback<T> {
        /**
         * Result callback
         *
         * @param result Result
         */
        void onResult(T result);
    }
}
