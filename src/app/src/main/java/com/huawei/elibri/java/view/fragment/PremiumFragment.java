/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.elibri.R;
import com.huawei.elibri.java.iap.ExceptionHandle;
import com.huawei.elibri.java.iap.IapApiCallback;
import com.huawei.elibri.java.iap.IapRequestHelper;
import com.huawei.elibri.java.iap.SubscriptionContract;
import com.huawei.elibri.java.iap.SubscriptionPresenter;
import com.huawei.elibri.java.iap.SubscriptionUtils;
import com.huawei.elibri.java.utility.Util;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Premium fragment for iap
 *
 * @author lWX916345
 * @since 25-10-2020
 */
public class PremiumFragment extends BaseFragment implements SubscriptionContract.View {
    private static final String[] SUBSCRIPTION_PRODUCT = new String[]{"p11", "p12", "p13"};
    private final String TAG = PremiumFragment.class.getName();
    private View viewfrag;
    private SubscriptionContract.Presenter presenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        queryIsReady();
        viewfrag = inflater.inflate(R.layout.fragment_premium, container, false);
        List<String> list = Arrays.asList(SUBSCRIPTION_PRODUCT);
        LinearLayout mPremiumFragmentLl = viewfrag.findViewById(R.id.premium_fragment_ll);
        mPremiumFragmentLl.setBackgroundResource(R.color.white);
        presenter = new SubscriptionPresenter(this);
        presenter.load(list);
        return viewfrag;
    }

    /**
     * Initiating an isEnvReady request when entering the app.
     * Check if the account service country supports IAP.
     */
    private void queryIsReady() {
        Util.setProgressBar(getContext());
        IapClient mClient = Iap.getIapClient(getActivity());
        IapRequestHelper.isEnvReady(
                mClient,
                new IapApiCallback<IsEnvReadyResult>() {
                    @Override
                    public void onSuccess(IsEnvReadyResult result) {
                        Util.stopProgressBar();
                    }

                    @Override
                    public void onFail(Exception exception) {
                        Util.stopProgressBar();
                        AGConnectCrash.getInstance().log(Log.ERROR, TAG + " " + exception.getMessage());
                        ExceptionHandle.handle(getActivity(), exception);
                    }
                });
    }


    @Override
    public void showProducts(List<ProductInfo> product_infos) {
        if (product_infos == null) {
            Log.d(TAG, "showProducts productInfos is null");
            showToast(getString(R.string.external_error), getContext());
            return;
        }
        for (ProductInfo productInfo : product_infos) {
            Log.d(TAG, "showProducts productInfos is: " + productInfo.getProductName());
            showProduct(productInfo);
        }
    }

    /**
     * Display the product details
     *
     * @param productInfo of the product
     */
    private void showProduct(ProductInfo productInfo) {
        Log.d(TAG, "showProduct productInfo: " + productInfo.getProductName());
        View view = getView(productInfo.getProductId(), viewfrag);
        if (view != null) {
            TextView productName = view.findViewById(R.id.product_name);
            TextView productDesc = view.findViewById(R.id.product_desc);
            TextView price = view.findViewById(R.id.price);
            productName.setText(productInfo.getProductName());
            productDesc.setText(productInfo.getProductDesc());
            price.setText(productInfo.getPrice());
        }
    }

    /**
     * Update the product purchase status
     *
     * @param ownedPurchasesResult Purchases result is the product to check it is subscribed or not
     */
    @Override
    public void updateProductStatus(OwnedPurchasesResult ownedPurchasesResult) {
        Log.d(TAG, "updateProductStatus ownedPurchasesResult getErrMsg: ");
        for (String productId : SUBSCRIPTION_PRODUCT) {
            Log.d(TAG, "updateProductStatus SUBSCRIPTION_PRODUCT active_plan");
            View view = getView(productId, viewfrag);
            Button button = view.findViewById(R.id.action);
            button.setTag(productId);
            if (SubscriptionUtils.shouldOfferService(ownedPurchasesResult, productId)) {
                Log.d(TAG, "updateProductStatus SUBSCRIPTION_PRODUCT active_plan: " + productId);
                button.setOnClickListener(getDetailActionListener());
            } else {
                button.setOnClickListener(getBuyActionListener());
            }
        }
    }

    /**
     * Create the view to display product information
     *
     * @param productId is the id of the product
     * @param parentView is the view
     * @return the view
     */
    private View getView(String productId, View parentView) {
        Log.d(TAG, "updateProductStatus getView ");
        View view = null;
        if (SUBSCRIPTION_PRODUCT[0].equals(productId)) {
            view = parentView.findViewById(R.id.service_one_product_one);
        }
        if (SUBSCRIPTION_PRODUCT[1].equals(productId)) {
            view = parentView.findViewById(R.id.service_one_product_two);
        }
        if (SUBSCRIPTION_PRODUCT[2].equals(productId)) {
            view = parentView.findViewById(R.id.service_one_product_three);
        }
        return view;
    }

    /**
     * On click of product purchase button this method will be called
     *
     * @return view of the clicked product
     */
    private View.OnClickListener getBuyActionListener() {
        return view -> {
            Log.d(TAG, "getBuyActionListener: ");
            Object data = view.getTag();
            if (data instanceof String) {
                Log.d(TAG, "getBuyActionListener productId: " + (String) data);
                String productId = (String) data;
                presenter.buy(productId);
            }
        };
    }

    /**
     * Get the detail of the product
     *
     * @return view
     */
    private View.OnClickListener getDetailActionListener() {
        return view -> {
            Log.d(TAG, "getDetailActionListener ");
            Object data = view.getTag();
            if (data instanceof String) {
                Log.d(TAG, "getDetailActionListener productId: " + (String) data);
                String productId = (String) data;
                presenter.showSubscription(productId);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        showAlertExitDialog(getView());
    }
}
