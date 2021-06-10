/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.service.model.Interest;
import com.huawei.elibri.java.service.model.InterestModel;
import com.huawei.elibri.java.service.model.ProfileInterest;
import com.huawei.elibri.java.service.repository.CloudDbConstant;
import com.huawei.elibri.java.service.repository.CloudDbQueries;
import com.huawei.elibri.java.utility.DialogUtils;
import com.huawei.elibri.java.view.activities.MainActivity;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.push.HmsMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Base class for Profile and Interests List
 *
 * @author: nWX914751
 * @since: 16-12-2020
 */
public class InterestBaseFragment extends BaseFragment {
    /**
     * Class tag for interest base class
     */
    protected final String TAG = InterestBaseFragment.class.getName();
    /**
     * Cloud db zone object
     */
    protected CloudDBZone mCloudDBZone;
    /**
     * Interest list declaration
     */
    protected List<InterestModel> interests;
    /**
     * last interest id inserted into cloud Db
     */
    protected int interestMaxId;

    /**
     * Fetches user interests
     *
     * @param rootView : rootView
     */
    protected void getUserInterests(View rootView) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        SavedPreference sp = SavedPreference.getInstance(getContext());
        Task<CloudDBZoneSnapshot<ProfileInterest>> queryTask =
                CloudDbQueries.getInstance().getUserInterestsQuery(sp, mCloudDBZone);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            HashMap<String, ProfileInterest> profileResult =
                                    processProfileInterestQueryResult(snapshot);
                            queryInterestList(rootView, profileResult);
                        })
                .addOnFailureListener(e -> Log.e(TAG, "addOnFailureListener getUserInterests"));
    }

    /**
     * Create HashMap from db data
     *
     * @param snapshot DB results
     * @return the resultant HashMap
     */
    private HashMap<String, ProfileInterest> processProfileInterestQueryResult(
            CloudDBZoneSnapshot<ProfileInterest> snapshot) {
        CloudDBZoneObjectList<ProfileInterest> bookInfoCursor = snapshot.getSnapshotObjects();
        HashMap<String, ProfileInterest> interest = new HashMap<>();
        try {
            while (bookInfoCursor.hasNext()) {
                ProfileInterest userInterests = bookInfoCursor.next();
                interest.put(userInterests.getInterestId(), userInterests);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, e);
        } finally {
            snapshot.release();
        }
        return interest;
    }

    /**
     * Get the last interest id inserted into cloud db table
     */
    protected void queryMaxInterestId() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<ProfileInterest>> queryTask =
                CloudDbQueries.getInstance().getMaxInterestIdQuery(mCloudDBZone);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            CloudDBZoneObjectList<ProfileInterest> bookInfoCursor = snapshot.getSnapshotObjects();
                            try {
                                while (bookInfoCursor.hasNext()) {
                                    ProfileInterest interest = bookInfoCursor.next();
                                    interestMaxId = interest.getId();
                                }
                            } catch (AGConnectCloudDBException e) {
                                Log.w(TAG, e);
                            } finally {
                                snapshot.release();
                            }
                        })
                .addOnFailureListener(e -> Log.w(TAG, e));
    }

    /**
     * List of interests
     *
     * @param view          RootView
     * @param ProfileResult users interests
     */
    public void queryInterestList(View view, HashMap<String, ProfileInterest> ProfileResult) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<Interest>> queryTask =
                mCloudDBZone.executeQuery(CloudDBZoneQuery.where(Interest.class), CloudDbConstant.POLICY);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            Log.d(TAG, "Fetch user interest data");
                            interests = processQueryResult(snapshot, ProfileResult);
                            showInterestList(view);
                        })
                .addOnFailureListener(e -> Log.w(TAG, e));
    }

    /**
     * Show the list of interest
     *
     * @param view of the class
     */
    protected void showInterestList(View view) {
    }

    /**
     * Get the interest list from cloud database
     *
     * @param snapshot       of the interest table
     * @param profile_result user profile data
     * @return interest list
     */
    private List<InterestModel> processQueryResult(
            CloudDBZoneSnapshot<Interest> snapshot, HashMap<String, ProfileInterest> profile_result) {
        CloudDBZoneObjectList<Interest> bookInfoCursor = snapshot.getSnapshotObjects();
        List<InterestModel> bookInfoList = new ArrayList<>(10);
        try {
            while (bookInfoCursor.hasNext()) {
                Interest interest = bookInfoCursor.next();
                if (profile_result.containsKey(String.valueOf(interest.getId()))) {
                    bookInfoList.add(
                            new InterestModel(
                                    profile_result.get(String.valueOf(interest.getId()))
                                            .getId(), interest, true, profile_result
                                    .get(String.valueOf(interest.getId()))));
                } else {
                    bookInfoList.add(new InterestModel(0, interest, false, null));
                }
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, e);
        } finally {
            snapshot.release();
        }
        return bookInfoList;
    }

    /**
     * Subscribe to a topic to receive notifications
     *
     * @param topic   is the interest which user want to subscribe
     * @param context of the class
     */
    void subscribe(String topic, Context context) {
        try {
            HmsMessaging.getInstance(context)
                    .subscribe(topic)
                    .addOnCompleteListener(
                            task -> {
                            });
        } catch (UnsupportedOperationException exception) {
            Log.e("TAG", "subscribe failed: exception");
        }
    }

    /**
     * UnSubscribe to a topic to receive notifications
     *
     * @param topic   is the interest which user want to unsubscribe
     * @param context of the class
     */
    void unsubscribe(String topic, Context context) {
        try {
            HmsMessaging.getInstance(context)
                    .unsubscribe(topic)
                    .addOnCompleteListener(
                            task -> {
                            });
        } catch (UnsupportedOperationException exception) {
            Log.e("TAG", "subscribe failed: exception");
        }
    }

    /**
     * Delete the user interest
     *
     * @param deleteProfileInterest delete interest from interest table
     */
    void deleteInterest(List<ProfileInterest> deleteProfileInterest) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> deleteTask = mCloudDBZone.executeDelete(deleteProfileInterest);
        deleteTask.addOnSuccessListener(
                integer -> {
                    redirectToNextScreen();
                    Log.d(TAG, "User interest deleted successfully");
                })
                .addOnFailureListener(exception -> showToast("Exception occured", getContext()));
    }

    /**
     * Redirects to next screen
     */
    protected void redirectToNextScreen() {
        DialogUtils.showPositiveAlertDialog(
                getActivity(),
                getString(R.string.message_data_saved),
                () -> {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                });
    }


    /**
     * Inserts Profile data
     */
    public void upsertInterests() {
        ArrayList<ProfileInterest> profileInterest = new ArrayList<>(10);
        ArrayList<ProfileInterest> deleteProfileInterest = new ArrayList<>(10);
        SavedPreference sp = SavedPreference.getInstance(getContext());
        for (InterestModel m : interests) {
            ProfileInterest interest = new ProfileInterest();
            interest.setEmail(sp.fetchEmailId());
            interest.setInterestId(String.valueOf(m.getUserInterests().getId()));
            if (m.isSelected()) {
                if (m.getId() > 0) {
                    interest.setId(m.getId());
                } else {
                    interestMaxId++;
                    interest.setId(interestMaxId);
                }
                Log.d(TAG, "User interest inserted successfully");
                profileInterest.add(interest);
                subscribe(m.getUserInterests().getInterest().toLowerCase(Locale.ROOT), getActivity());
            } else if (!m.isSelected() && m.getId() > 0) {
                interest.setId(m.getId());
                deleteProfileInterest.add(m.getInterest());
                unsubscribe(m.getUserInterests().getInterest().toLowerCase(Locale.ROOT), getActivity());
            } else {
                continue;
            }
        }
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(profileInterest);
        upsertTask
                .addOnSuccessListener(
                        cloudDBZoneResult -> {
                            if (!deleteProfileInterest.isEmpty()) {
                                deleteInterest(deleteProfileInterest);

                            } else {
                                redirectToNextScreen();
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Log.d(TAG, "Delete failed");
                        });
    }

    @Override
    public void onResume() {
        super.onResume();
        showAlertExitDialog(getView());
    }
}
