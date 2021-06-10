/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.utility.Util;
import com.huawei.elibri.java.adapter.InterestAdapter;

/**
 * Displays user Profile (name, email, interest, etc)
 *
 * @author: nWX914751
 * @since: 02-11-2020
 */
public class InterestFragment extends InterestBaseFragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interest, container, false);
        Button save = view.findViewById(R.id.button_save);
        save.setOnClickListener(this);
        initializeUi(view);
        return view;
    }

    /**
     * Initializes UI
     *
     * @param view : rootView
     */
    protected void initializeUi(View view) {
        Util.setProgressBar(getContext());
        initializeDb(new DBOpenInterface() {
            @Override
            public void success(CloudDBZone cloudDBZone) {
                Util.stopProgressBar();
                mCloudDBZone = cloudDBZone;
                getUserInterests(view);
                queryMaxInterestId();
            }

            @Override
            public void failure() {
                Util.stopProgressBar();
            }
        });
    }

    /**
     * Show interests in UI
     *
     * @param view : RootView
     */
    @Override
    protected void showInterestList(View view) {
        SavedPreference sp = SavedPreference.getInstance(getActivity().getApplication());
        RecyclerView mRecyclerView = view.findViewById(R.id.rv_interests);
        InterestAdapter mAdapter = new InterestAdapter(interests, false);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_save) {
            upsertInterests();
        }
    }
}
