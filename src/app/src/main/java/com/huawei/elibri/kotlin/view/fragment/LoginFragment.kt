/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.utility.Constants
import com.huawei.elibri.kotlin.utility.Util.isOnline
import com.huawei.elibri.kotlin.utility.Util.replaceFragment
import com.huawei.hmf.tasks.Task
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import org.json.JSONException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Login Functionality
 *
 * @author: nWX914751
 * @since: 21-12-2020
 */
class LoginFragment : BaseFragment() {
    private val TAG = LoginFragment::class.java.name
    var scopeList: List<Scope> = ArrayList()

    val user = AGConnectAuth.getInstance().currentUser
    private var mAuthManager: AccountAuthService? = null
    private var mAuthParam: AccountAuthParams? = null
    private var service: HuaweiIdAuthService? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_login, container, false)
        isOnline(activity!!.applicationContext)
        initializeUi(view)
        return view
    }

    /**
     * Initializes UI
     *
     * @param view_frag of the login fragment
     */
    private fun initializeUi(view_frag: View) {
        val btnHuawei = view_frag.findViewById<Button>(R.id.btnHuaweiSignIn)
        token
        btnHuawei.setOnClickListener { view: View? ->

            val huaweiIdAuthParamsHelper =
                HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            var scopeList: ArrayList<Scope> = ArrayList()

            scopeList.add(Scope("https://www.huawei.com/auth/account/base.profile"))
            scopeList.add(Scope("email"))
            huaweiIdAuthParamsHelper.setScopeList(scopeList)
            val authParams: HuaweiIdAuthParams =
                huaweiIdAuthParamsHelper.setAccessToken().createParams()
            val service: HuaweiIdAuthService =
                HuaweiIdAuthManager.getService(context, authParams)
            startActivityForResult(service?.signInIntent, 101)

            /*  mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setId().setEmail()
                  .setIdToken()
                  .setAccessToken()
                  .createParams()
              mAuthManager = AccountAuthManager.getService(context, mAuthParam, 1)
              startActivityForResult(mAuthManager?.signInIntent, *//*Constant.REQUEST_SIGN_IN_LOGIN101*//*101)*/
        }

    }

    /**
     * Gets Notification token
     */
    private val token: Unit
        private get() {
            ThreadPoolExecutor(
                5,
                5,
                5,
                TimeUnit.SECONDS,
                LinkedBlockingQueue()
            ).execute {
                try {
                    val appId = AGConnectServicesConfig.fromContext(activity)
                        .getString("client/app_id")
                    val pushtoken =
                        HmsInstanceId.getInstance(activity).getToken(appId, "HCM")
                    if (!TextUtils.isEmpty(pushtoken)) {
                        Log.d(TAG, "get token: *************")
                    }
                } catch (exception: ApiException) {
                    Log.d(TAG, "Get token failed")
                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === 101) {
            val authHuaweiIdTask: Task<AuthHuaweiId> =
                HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful()) {
                val huaweiAccount: AuthHuaweiId = authHuaweiIdTask.getResult()
                Log.i(
                    TAG,
                    "email:" + huaweiAccount.email + "name : " + huaweiAccount.displayName
                )
                val credential: AGConnectAuthCredential
                credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
                AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener { signInResult: SignInResult? ->

                        if (data!!.extras != null) {
                            try {
                                val sp = SavedPreference.getInstance(activity!!.applicationContext)

                                sp?.name = huaweiAccount.getDisplayName()
                                sp?.saveEmailId(huaweiAccount.email)

                            } catch (exception: JSONException) {
                                Log.e(TAG, "AGC error")
                            }
                            val bundle = Bundle()
                            bundle.putString(
                                Constants.TAG,
                                Constants.PROFILE_FRAGMENT
                            )
                            replaceFragment(activity!!, ProfileFragment(), bundle)

                        }
                    }
                    .addOnFailureListener { e: Exception ->


                    }

            }
        } else {
            Log.i(TAG, "signIn failed")
        }
    }
}


