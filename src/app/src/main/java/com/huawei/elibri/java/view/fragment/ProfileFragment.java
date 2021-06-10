/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.adapter.InterestAdapter;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.service.model.Profile;
import com.huawei.elibri.java.service.repository.CloudDbQueries;
import com.huawei.elibri.java.utility.DialogUtils;
import com.huawei.elibri.java.utility.Util;
import com.huawei.hmf.tasks.Task;

import java.util.HashMap;

/**
 * Displays user Profile (name, email, interest, etc)
 *
 * @author: nWX914751
 * @since: 02-11-2020
 */
public class ProfileFragment extends InterestBaseFragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button save = view.findViewById(R.id.button_save);
        save.setOnClickListener(this);
        initializeUi(view);
        return view;
    }

    /**
     * Initializes UI
     *
     * @param view rootView
     */
    private void initializeUi(View view) {
        Util.setProgressBar(getContext());
        initializeDb(new DBOpenInterface() {
            @Override
            public void success(CloudDBZone cloudDBZone) {
                Util.stopProgressBar();
                mCloudDBZone = cloudDBZone;
                checkIfUserExists(view);
                queryMaxInterestId();
            }

            @Override
            public void failure() {
                Util.stopProgressBar();
            }
        });
    }

    /**
     * Checks if user data exists in DB or not
     *
     * @param rootView : rootView
     */
    private void checkIfUserExists(View rootView) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        SavedPreference sp = SavedPreference.getInstance(getContext());
        Task<CloudDBZoneSnapshot<Profile>> queryTask = CloudDbQueries.getInstance().getUserProfile(sp, mCloudDBZone);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            inflateUserData(rootView, snapshot);
                            Log.d(TAG, "Fetch user profile data");
                        })
                .addOnFailureListener(Throwable::getLocalizedMessage);
    }

    /**
     * Inflates user data in UI
     *
     * @param rootView view
     * @param snapshot query result
     */
    private void inflateUserData(View rootView, CloudDBZoneSnapshot<Profile> snapshot) {
        Profile profileResult = processProfileQueryResult(snapshot);
        if (profileResult != null) {
            RadioGroup gender = rootView.findViewById(R.id.rg_gender);
            TextInputEditText age = rootView.findViewById(R.id.tiet_age);
            age.setText(String.valueOf(profileResult.getAge()));
            if (profileResult.getGender()) {
                gender.check(R.id.radio_female);
            } else {
                gender.check(R.id.radio_male);
            }
            gender.setEnabled(false);
            getUserInterests(rootView);
            SavedPreference.getInstance(getContext().getApplicationContext())
                    .isSubscribed(profileResult.getSubscription());
        } else {
            queryInterestList(rootView, new HashMap<>());
        }
    }

    /**
     * Returns user Profile
     *
     * @param snapshot : Cloud DB result
     * @return user Profile
     */
    private Profile processProfileQueryResult(CloudDBZoneSnapshot<Profile> snapshot) {
        CloudDBZoneObjectList<Profile> bookInfoCursor = snapshot.getSnapshotObjects();
        Profile interest = null;
        try {
            while (bookInfoCursor.hasNext()) {
                interest = bookInfoCursor.next();
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, e);
        } finally {
            snapshot.release();
        }
        return interest;
    }


    /**
     * Show interests in UI
     *
     * @param view : RootView
     */
    @Override
    protected void showInterestList(View view) {
        SavedPreference sp = SavedPreference.getInstance(getContext());
        RecyclerView mRecyclerView = view.findViewById(R.id.rv_interests);
        InterestAdapter mAdapter = new InterestAdapter(interests, true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        TextView name = view.findViewById(R.id.tv_name);
        name.setText(sp.getName());
        TextView emailId = view.findViewById(R.id.tv_emailid);
        emailId.setText(sp.fetchEmailId());
    }


    /**
     * Inserts user Profile data
     *
     * @param rootView parent view
     */
    private void upsertBookInfos(View rootView) {
        RadioGroup gender = rootView.findViewById(R.id.rg_gender);
        SavedPreference sp = SavedPreference.getInstance(getContext());
        Profile profile = new Profile();
        TextInputEditText age = rootView.findViewById(R.id.tiet_age);
        if (!TextUtils.isEmpty(age.getText())) {
            profile.setAge(Byte.parseByte(age.getText().toString()));
        }
        profile.setName(sp.getName());
        profile.setGender(gender.getCheckedRadioButtonId() == R.id.radio_female);
        boolean subscription = sp.getIsSubscribed();
        profile.setSubscription(subscription);
        profile.setEmailid(sp.fetchEmailId());
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(profile);
        upsertTask
                .addOnSuccessListener(cloudDBZoneResult -> {
                    upsertInterests();
                    Log.d(TAG, "Profile data added successfully");
                })
                .addOnFailureListener(
                        e -> {
                            DialogUtils.showPositiveAlertDialog(
                                    getActivity(), e.getLocalizedMessage(), () -> getActivity().finishAffinity());
                            e.getLocalizedMessage();
                        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_save) {
            upsertBookInfos(view.getRootView());
        }
    }

    /**
     * It will ask to user does he wants to exit the app
     */
    public void onResume() {
        super.onResume();
        showAlertExitDialog(getView());
    }
}
