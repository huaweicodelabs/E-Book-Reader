/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.iap.*
import com.huawei.elibri.kotlin.iap.SubscriptionContract.Presenter
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.IsEnvReadyResult
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfo
import java.util.*

/**
 * Premium fragment for iap
 *
 * @author lWX916345
 * @since 25-10-2020
 */
class PremiumFragment() : BaseFragment(), SubscriptionContract.View {
    private val TAG: String = PremiumFragment::class.java.name
    private var viewfrag: View? = null
    private var presenter: Presenter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        queryIsReady()
        viewfrag = inflater.inflate(R.layout.fragment_premium, container, false)
        val list: List<String> =
            Arrays.asList(*SUBSCRIPTION_PRODUCT)
        val mPremiumFragmentLl: LinearLayout = viewfrag?.findViewById(R.id.premium_fragment_ll)!!
        mPremiumFragmentLl.setBackgroundResource(R.color.white)
        presenter = SubscriptionPresenter(this)
        (presenter as SubscriptionPresenter).load(list)
        return viewfrag
    }

    /**
     * Initiating an isEnvReady request when entering the app.
     * Check if the account service country supports IAP.
     */
    private fun queryIsReady() {
        setProgressBar(context)
        val mClient: IapClient = Iap.getIapClient(activity)
        IapRequestHelper.isEnvReady(
            mClient,
            object : IapApiCallback<IsEnvReadyResult?> {

                override fun onFail(exception: Exception) {
                    stopProgressBar()
                    AGConnectCrash.getInstance()
                        .log(Log.ERROR, TAG + " " + exception.message)
                    ExceptionHandle.handle(activity, exception)
                }

                override fun onSuccess(result: IsEnvReadyResult?) {
                    stopProgressBar()
                }
            })
    }

    override fun showProducts(product_infos: List<ProductInfo>) {
        if (product_infos == null) {
            Log.d(TAG, "showProducts productInfos is null")
            showToast(getString(R.string.external_error), context)
            return
        }
        for (productInfo: ProductInfo in product_infos) {
            Log.d(TAG, "showProducts productInfos is: " + productInfo.productName)
            showProduct(productInfo)
        }
    }

    /**
     * Display the product details
     *
     * @param productInfo of the product
     */
    private fun showProduct(productInfo: ProductInfo) {
        Log.d(TAG, "showProduct productInfo: " + productInfo.productName)
        val view: View? = getView(productInfo.productId, viewfrag)
        if (view != null) {
            val productName: TextView = view.findViewById(R.id.product_name)
            val productDesc: TextView = view.findViewById(R.id.product_desc)
            val price: TextView = view.findViewById(R.id.price)
            productName.text = productInfo.productName
            productDesc.text = productInfo.productDesc
            price.text = productInfo.price
        }
    }

    /**
     * Update the product purchase status
     *
     * @param ownedPurchasesResult Purchases result is the product to check it is subscribed or not
     */
    override fun updateProductStatus(ownedPurchasesResult: OwnedPurchasesResult) {
        Log.d(TAG, "updateProductStatus ownedPurchasesResult getErrMsg: ")
        for (productId: String in SUBSCRIPTION_PRODUCT) {
            Log.d(TAG, "updateProductStatus SUBSCRIPTION_PRODUCT active_plan")
            val view: View? = getView(productId, viewfrag)
            val button: Button =
                view!!.findViewById(R.id.action)
            button.tag = productId
            if (SubscriptionUtils.shouldOfferService(ownedPurchasesResult, productId)) {
                Log.d(
                    TAG,
                    "updateProductStatus SUBSCRIPTION_PRODUCT active_plan: $productId"
                )
                button.setOnClickListener(detailActionListener)
            } else {
                button.setOnClickListener(buyActionListener)
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
    private fun getView(
        productId: String,
        parentView: View?
    ): View? {
        Log.d(TAG, "updateProductStatus getView ")
        var view: View? = null
        if ((SUBSCRIPTION_PRODUCT.get(0) == productId)) {
            view = parentView!!.findViewById(R.id.service_one_product_one)
        }
        if ((SUBSCRIPTION_PRODUCT.get(1) == productId)) {
            view = parentView!!.findViewById(R.id.service_one_product_two)
        }
        if ((SUBSCRIPTION_PRODUCT.get(2) == productId)) {
            view = parentView!!.findViewById(R.id.service_one_product_three)
        }
        return view
    }

    /**
     * On click of product purchase button this method will be called
     *
     * @return view of the clicked product
     */
    private val buyActionListener: View.OnClickListener
        private get() = View.OnClickListener { view: View ->
            Log.d(TAG, "getBuyActionListener: ")
            val data: Any = view.getTag()
            if (data is String) {
                Log.d(TAG, "getBuyActionListener productId: " + data)
                presenter!!.buy(data)
            }
        }

    /**
     * Get the detail of the product
     *
     * @return view
     */
    private val detailActionListener: View.OnClickListener
        private get() {
            return View.OnClickListener { view: View ->
                Log.d(TAG, "getDetailActionListener ")
                val data: Any = view.getTag()
                if (data is String) {
                    Log.d(
                        TAG,
                        "getDetailActionListener productId: " + data
                    )
                    presenter!!.showSubscription(data)
                }
            }
        }

    override fun onResume() {
        super.onResume()
        showAlertExitDialog((view)!!)
    }

    companion object {
        private val SUBSCRIPTION_PRODUCT: Array<String> =
            arrayOf("p11", "p12", "p13")
    }
}