/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.agconnect.datastore.annotation.SharedPreference;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.utility.Constants;
import com.huawei.elibri.java.utility.Util;
import com.huawei.elibri.java.view.activities.LoginActivity;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.huawei.elibri.java.utility.Util.isOnline;

/**
 * Login Functionality
 *
 * @author: nWX914751
 * @since: 21-12-2020
 */
public class LoginFragment extends BaseFragment {
    private final String TAG = LoginFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        isOnline(getActivity().getApplicationContext());
        initializeUi(view);
        return view;
    }

    /**
     * Initializes UI
     *
     * @param view_frag of the login fragment
     */
    private void initializeUi(View view_frag) {
        Button btnHuawei = view_frag.findViewById(R.id.btnHuaweiSignIn);

        getToken();
        btnHuawei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setEmail();
                List<Scope> scopeList = new ArrayList<>();
                scopeList.add(new Scope("https://www.huawei.com/auth/account/base.profile"));
                scopeList.add(new Scope("email"));
                huaweiIdAuthParamsHelper.setScopeList(scopeList);
                HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams();
                HuaweiIdAuthService service = HuaweiIdAuthManager.getService(getActivity(), authParams);
                startActivityForResult(service.getSignInIntent(), 101);


            }
        });


    }

    /**
     * Saves user data in Shared Preferences
     *
     * @throws JSONException
     */
  /*  private void saveUserData() throws JSONException {
        SavedPreference sp = SavedPreference.getInstance(getActivity().getApplicationContext());
        String jsonStr = AGCConfiguration.getInstance().getSetUserData(getActivity());
        JSONObject reader = new JSONObject(jsonStr);
        JSONObject profileData = reader.getJSONObject("profile");
        AGConnectCrash.getInstance().setUserId(profileData.getString("email"));
        if (profileData.has("name")) {
            sp.setName(profileData.getString("name"));
        }
        if (profileData.has("email")) {
            sp.saveEmailId(profileData.getString("email"));
        }
    }

  */

    /**
     * Gets Notification token
     */
    private void getToken() {
        new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(getActivity()).getString("client/app_id");
                    String pushtoken = HmsInstanceId.getInstance(getActivity()).getToken(appId, "HCM");
                    if (!TextUtils.isEmpty(pushtoken)) {
                        Log.d(TAG, "get token: *************");
                    }
                } catch (ApiException exception) {
                    Log.d(TAG, "Get token failed");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 101) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken());
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {

                        AGConnectUser user =  AGConnectAuth.getInstance().getCurrentUser();

                        SavedPreference sp = SavedPreference.getInstance(getActivity().getApplicationContext());
                        sp.setName(user.getDisplayName());
                        sp.saveEmailId(huaweiAccount.getEmail());


                        if(data.getExtras()!= null){
                            sp.setName(huaweiAccount.getDisplayName());
                            sp.saveEmailId(huaweiAccount.getEmail());

                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.TAG, Constants.PROFILE_FRAGMENT);
                            Util.replaceFragment(getActivity(),new ProfileFragment(), bundle);
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // onFail
                    }
                });
            }
        }



       /* if (resultCode == Activity.RESULT_OK) {
            if (data.getExtras() != null) {
                try {
                    saveUserData();
                } catch (JSONException exception) {
                    Log.e(TAG, "AGC error");
                }
                Log.e(TAG, "RESPONSE IS " + data.getExtras().getString(Constant.RESPONSE));
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG, Constants.PROFILE_FRAGMENT);
                Util.replaceFragment(getActivity(), new ProfileFragment(), bundle);
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            showToast(getString(R.string.error), getActivity());
        }*/
    }
}
