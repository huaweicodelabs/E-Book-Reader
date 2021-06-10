/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.adapter.InterestAdapter
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.interfaces.DialogClickInterface
import com.huawei.elibri.kotlin.service.model.Profile
import com.huawei.elibri.kotlin.service.repository.CloudDbQueries
import com.huawei.elibri.kotlin.utility.DialogUtils.showPositiveAlertDialog
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.hmf.tasks.Task


/**
 * Displays user Profile (name, email, interest, etc)
 *
 * @author: nWX914751
 * @since: 02-11-2020
 */
class ProfileFragment : InterestBaseFragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val save = view.findViewById<Button>(R.id.button_save)
        save.setOnClickListener(this)
        initializeUi(view)
        return view
    }

    /**
     * Initializes UI
     *
     * @param view rootView
     */
    private fun initializeUi(view: View) {
        setProgressBar(context)
        initializeDb(object : DBOpenInterface {
            override fun success(cloudDBZone: CloudDBZone?) {
                stopProgressBar()
                mCloudDBZone = cloudDBZone
                Log.d("Cloiud Db : ","Cloud DBZone " + mCloudDBZone)
                queryMaxInterestId()
                checkIfUserExists(view)

            }

            override fun failure() {
                stopProgressBar()
            }
        })
    }

    /**
     * Checks if user data exists in DB or not
     *
     * @param rootView : rootView
     */
    private fun checkIfUserExists(rootView: View) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val sp = SavedPreference.getInstance(context!!)
        val queryTask: Task<CloudDBZoneSnapshot<Profile>> = CloudDbQueries.instance!!.getUserProfile(sp!!, mCloudDBZone!!)
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Profile> ->
                inflateUserData(rootView, snapshot)
                Log.d(TAG, "Fetch user profile data")
            }
            .addOnFailureListener {
                    obj: Exception -> obj.localizedMessage
            }
    }

    /**
     * Inflates user data in UI
     *
     * @param rootView view
     * @param snapshot query result
     */
    private fun inflateUserData(
        rootView: View,
        snapshot: CloudDBZoneSnapshot<Profile>
    ) {
        val profileResult =
            processProfileQueryResult(snapshot)
        if (profileResult != null) {
            val gender = rootView.findViewById<RadioGroup>(R.id.rg_gender)
            val age: TextInputEditText = rootView.findViewById(R.id.tiet_age)
            age.setText(profileResult.age.toString())
            if (profileResult.gender) {
                gender.check(R.id.radio_female)
            } else {
                gender.check(R.id.radio_male)
            }
            gender.isEnabled = false
            getUserInterests(rootView)
            SavedPreference.getInstance(context!!.applicationContext)!!.isSubscribed(profileResult.subscription)
        } else {
          //  queryInterestList(rootView, HashMap())
        }
    }

    /**
     * Returns user Profile
     *
     * @param snapshot : Cloud DB result
     * @return user Profile
     */
    private fun processProfileQueryResult(snapshot: CloudDBZoneSnapshot<Profile>): Profile? {
        val bookInfoCursor =
            snapshot.snapshotObjects
        var interest: Profile? = null
        try {
            while (bookInfoCursor.hasNext()) {
                interest = bookInfoCursor.next()
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, e)
        } finally {
            snapshot.release()
        }
        return interest
    }

    /**
     * Show interests in UI
     *
     * @param view : RootView
     */
    override fun showInterestList(view: View?) {
        val sp = SavedPreference.getInstance(context!!)
        val mRecyclerView: RecyclerView = view!!.findViewById(R.id.rv_interests)
        val mAdapter = InterestAdapter(interests, true)
        val manager = LinearLayoutManager(activity)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mRecyclerView.adapter = mAdapter
        val name = view.findViewById<TextView>(R.id.tv_name)
        name.text = sp!!.name
        val emailId = view.findViewById<TextView>(R.id.tv_emailid)
        emailId.text = sp.fetchEmailId()
    }

    /**
     * Inserts user Profile data
     *
     * @param rootView parent view
     */
    private fun upsertBookInfos(rootView: View) {
        val gender = rootView.findViewById<RadioGroup>(R.id.rg_gender)
        val sp = SavedPreference.getInstance(context!!)
        val profile = Profile()
        val age: TextInputEditText = rootView.findViewById(R.id.tiet_age)
        if (!TextUtils.isEmpty(age.text)) {
            profile.age = age.text.toString().toByte()
        }
        profile.name = sp!!.name
        profile.gender = gender.checkedRadioButtonId == R.id.radio_female
        val subscription: Boolean = sp.isSubscribed
        profile.subscription = subscription
        profile.emailid = sp!!.fetchEmailId()
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        Log.d("ProfileFragment","Profile : "+profile.name+profile.age+profile.emailid+profile.gender+profile.subscription)
        if (AGConnectAuth.getInstance().currentUser!=null ){
            val upsertTask: Task<Int> = mCloudDBZone!!.executeUpsert(profile)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                upsertInterests()
            }.addOnFailureListener {e: java.lang.Exception ->
                Log.d("ProfileFragment","exception : "+e)
                showPositiveAlertDialog(
                    activity!!,
                    e.localizedMessage,
                    object : DialogClickInterface {
                        override fun positiveButton() {
                            activity!!.finishAffinity()
                        }
                    }
                )
        }

            }else{
            Log.d("Cloud DB","Not opened");
        }

    }

    override fun onClick(view: View) {
        if (view.id == R.id.button_save) {
            upsertBookInfos(view.rootView)
        }
    }

    /**
     * It will ask to user does he wants to exit the app
     */
    override fun onResume() {
        super.onResume()
        showAlertExitDialog(view!!)
    }
}