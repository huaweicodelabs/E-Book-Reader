/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.interfaces.DialogClickInterface
import com.huawei.elibri.kotlin.service.model.Interest
import com.huawei.elibri.kotlin.service.model.InterestModel
import com.huawei.elibri.kotlin.service.model.ProfileInterest
import com.huawei.elibri.kotlin.service.repository.CloudDbConstant
import com.huawei.elibri.kotlin.service.repository.CloudDbQueries
import com.huawei.elibri.kotlin.utility.DialogUtils.showPositiveAlertDialog
import com.huawei.elibri.kotlin.view.activities.MainActivity
import com.huawei.hmf.tasks.Task
import com.huawei.hms.push.HmsMessaging
import java.util.*

/**
 * Base class for Profile and Interests List
 *
 * @author: nWX914751
 * @since: 16-12-2020
 */
open class InterestBaseFragment : BaseFragment() {
    /**
     * Class tag for interest base class
     */
    protected val TAG = InterestBaseFragment::class.java.name

    /**
     * Cloud db zone object
     */
    protected var mCloudDBZone: CloudDBZone? = null

    /**
     * Interest list declaration
     */
    protected var interests: List<InterestModel>? = null

    /**
     * last interest id inserted into cloud Db
     */
    protected var interestMaxId = 0

    /**
     * Fetches user interests
     *
     * @param rootView : rootView
     */
    protected fun getUserInterests(rootView: View?) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val sp = SavedPreference.getInstance(context!!)
        val queryTask: Task<CloudDBZoneSnapshot<ProfileInterest>> =
            CloudDbQueries.instance!!.getUserInterestsQuery(sp!!, mCloudDBZone!!)
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<ProfileInterest> ->
                val profileResult =
                    processProfileInterestQueryResult(snapshot)
                Log.d("List got from Cloud ",""+ profileResult.size)
                queryInterestList(rootView, profileResult)
            }
            .addOnFailureListener { e: Exception? ->
                Log.e(
                    TAG,
                    "addOnFailureListener getUserInterests"
                )
            }
    }

    /**
     * Create HashMap from db data
     *
     * @param snapshot DB results
     * @return the resultant HashMap
     */
    private fun processProfileInterestQueryResult(
        snapshot: CloudDBZoneSnapshot<ProfileInterest>
    ): HashMap<String?, ProfileInterest?> {
        val bookInfoCursor = snapshot.snapshotObjects
        val interest =
            HashMap<String?, ProfileInterest?>()
        try {
            while (bookInfoCursor.hasNext()) {
                val userInterests = bookInfoCursor.next()
                interest[userInterests.interestId] = userInterests
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, e)
        } finally {
            snapshot.release()
        }
        return interest
    }

    /**
     * Get the last interest id inserted into cloud db table
     */
    protected open fun queryMaxInterestId() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }


        val queryTask: Task<CloudDBZoneSnapshot<ProfileInterest>> = CloudDbQueries.instance!!.getMaxInterestIdQuery(mCloudDBZone!!)

        queryTask.addOnSuccessListener { snapshot: CloudDBZoneSnapshot<ProfileInterest> -> val bookInfoCursor =
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
     * List of interests
     *
     * @param view          RootView
     * @param ProfileResult users interests
     */
    fun queryInterestList(
        view: View?,
        ProfileResult: HashMap<String?, ProfileInterest?>
    ) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val queryTask =
            mCloudDBZone!!.executeQuery(
                CloudDBZoneQuery.where(
                    Interest::class.java
                ), CloudDbConstant.POLICY
            )
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Interest> ->
                Log.d(TAG, "Fetch user interest data")
                interests = processQueryResult(snapshot, ProfileResult)
                showInterestList(view)
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(
                    TAG,
                    e
                )
            }
    }

    /**
     * Show the list of interest
     *
     * @param view of the class
     */
    protected open fun showInterestList(view: View?) {}

    /**
     * Get the interest list from cloud database
     *
     * @param snapshot       of the interest table
     * @param profile_result user profile data
     * @return interest list
     */
    private fun processQueryResult(
        snapshot: CloudDBZoneSnapshot<Interest>,
        profile_result: HashMap<String?, ProfileInterest?>
    ): List<InterestModel> {
        val bookInfoCursor = snapshot.snapshotObjects
        val bookInfoList: MutableList<InterestModel> =
            ArrayList(10)
        try {
            while (bookInfoCursor.hasNext()) {
                val interest = bookInfoCursor.next()
                if (profile_result.containsKey(interest.id.toString())) {
                    bookInfoList.add(
                        InterestModel(
                            profile_result[interest.id.toString()]?.id!!,
                            interest,
                            true,
                            profile_result[interest.id.toString()]
                        )
                    )
                } else {
                    bookInfoList.add(InterestModel(0, interest, false, null))
                }
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, e)
        } finally {
            snapshot.release()
        }
        return bookInfoList
    }

    /**
     * Subscribe to a topic to receive notifications
     *
     * @param topic   is the interest which user want to subscribe
     * @param context of the class
     */
    fun subscribe(topic: String?, context: Context?) {
        try {
            HmsMessaging.getInstance(context)
                .subscribe(topic)
                .addOnCompleteListener { task: Task<Void?>? -> }
        } catch (exception: UnsupportedOperationException) {
            Log.e("TAG", "subscribe failed: exception")
        }
    }

    /**
     * UnSubscribe to a topic to receive notifications
     *
     * @param topic   is the interest which user want to unsubscribe
     * @param context of the class
     */
    fun unsubscribe(topic: String?, context: Context?) {
        try {
            HmsMessaging.getInstance(context)
                .unsubscribe(topic)
                .addOnCompleteListener { task: Task<Void?>? -> }
        } catch (exception: UnsupportedOperationException) {
            Log.e("TAG", "subscribe failed: exception")
        }
    }

    /**
     * Delete the user interest
     *
     * @param deleteProfileInterest delete interest from interest table
     */
    fun deleteInterest(deleteProfileInterest: List<ProfileInterest?>?) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val deleteTask =
            mCloudDBZone!!.executeDelete(deleteProfileInterest!!)
        deleteTask.addOnSuccessListener { integer: Int? ->
            redirectToNextScreen()
            Log.d(TAG, "User interest deleted successfully")
        }
            .addOnFailureListener { exception: Exception? ->
                showToast(
                    "Exception occured",
                    context
                )
            }
    }

    /**
     * Redirects to next screen
     */
    protected fun redirectToNextScreen() {
        showPositiveAlertDialog(
            activity!!,
            getString(R.string.message_data_saved),
            object : DialogClickInterface {
                override fun positiveButton() {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity!!.finish()
                }
            }
        )
    }

    /**
     * Inserts Profile data
     */
    fun upsertInterests() {
        val profileInterest =
            ArrayList<ProfileInterest>(10)
        val deleteProfileInterest =
            ArrayList<ProfileInterest?>(10)
        val sp = SavedPreference.getInstance(context!!)
        for (m in interests!!) {
            val interest = ProfileInterest()
            interest.email = sp!!.fetchEmailId()
            interest.interestId = m.userInterests.id.toString()
            if (m.isSelected) {
                if (m.id > 0) {
                    interest.id = m.id
                } else {
                    interestMaxId++
                    interest.id = interestMaxId
                }
                Log.d(TAG, "User interest inserted successfully")
                profileInterest.add(interest)
                subscribe(
                    m.userInterests.interest.toLowerCase(Locale.ROOT),
                    activity
                )
            } else if (!m.isSelected && m.id > 0) {
                interest.id = m.id
                deleteProfileInterest.add(m.interest)
                unsubscribe(
                    m.userInterests.interest.toLowerCase(Locale.ROOT),
                    activity
                )
            } else {
                continue
            }
        }
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(profileInterest)
        upsertTask
            .addOnSuccessListener { cloudDBZoneResult: Int? ->
                if (!deleteProfileInterest.isEmpty()) {
                    deleteInterest(deleteProfileInterest)
                } else {
                    redirectToNextScreen()
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.d(
                    TAG,
                    "Delete failed"
                )
            }
    }

    override fun onResume() {
        super.onResume()
        showAlertExitDialog(view!!)
    }
}