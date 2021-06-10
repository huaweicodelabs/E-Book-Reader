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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.adapter.InterestAdapter
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.service.model.ProfileInterest
import com.huawei.elibri.kotlin.service.repository.CloudDbQueries
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.hmf.tasks.Task

/**
 * Displays user Profile (name, email, interest, etc)
 *
 * @author: nWX914751
 * @since: 02-11-2020
 */
public class InterestFragment : InterestBaseFragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_interest, container, false)
        val save = view.findViewById<Button>(R.id.button_save)
        save.setOnClickListener(this)
        initializeUi(view)
        return view
    }

    /**
     * Initializes UI
     *
     * @param view : rootView
     */
    protected fun initializeUi(view: View?) {
        setProgressBar(context)
        initializeDb(object : DBOpenInterface {
            override fun success(cloudDBZone: CloudDBZone?) {
                stopProgressBar()
                mCloudDBZone = cloudDBZone
                getUserInterests(view)
                queryMaxInterestId()
            }

            override fun failure() {
                stopProgressBar()
            }
        })
    }

    /**
     * Get the last interest id inserted into cloud db table
     */
    protected override fun queryMaxInterestId() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val queryTask: Task<CloudDBZoneSnapshot<ProfileInterest>> =
            CloudDbQueries.instance!!.getMaxInterestIdQuery(mCloudDBZone!!)
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<ProfileInterest> ->
                val bookInfoCursor =
                    snapshot.snapshotObjects
                try {
                    while (bookInfoCursor.hasNext()) {
                        val interest = bookInfoCursor.next()
                        interestMaxId = interest.id!!
                    }
                } catch (e: AGConnectCloudDBException) {
                    Log.w(TAG, e)
                } finally {
                    snapshot.release()
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(
                    TAG,
                    e
                )
            }
    }

    /**
     * Show interests in UI
     *
     * @param view : RootView
     */
    override fun showInterestList(view: View?) {
        val sp = SavedPreference.getInstance(activity!!.application)
        val mRecyclerView: RecyclerView = view!!.findViewById(R.id.rv_interests)
        val mAdapter = InterestAdapter(interests, false)
        val manager = GridLayoutManager(activity, 2)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mRecyclerView.adapter = mAdapter
    }

    override fun onClick(view: View) {
        if (view.id == R.id.button_save) {
            upsertInterests()
        }
    }
}