/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.service.repository

import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneObject
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.service.model.Bookmark
import com.huawei.elibri.kotlin.service.model.Books
import com.huawei.elibri.kotlin.service.model.Profile
import com.huawei.elibri.kotlin.service.model.ProfileInterest
import com.huawei.hmf.tasks.Task


/**
 * @author: nWX914751
 * @since: 26-11-2020
 */
class CloudDbQueries {

    /**
     * Query to get the user data from cloud db table
     *
     * @param sp is shared preference instance
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    fun getUserProfile(sp: SavedPreference, mCloudDBZone: CloudDBZone): Task<CloudDBZoneSnapshot<Profile>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(Profile::class.java).equalTo("emailid", sp.fetchEmailId()),
            CloudDbConstant.POLICY
        )
    }

    /**
     * Query for user interests
     *
     * @param sp is shared preference instance
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    fun getUserInterestsQuery(
        sp: SavedPreference, mCloudDBZone: CloudDBZone
    ): Task<CloudDBZoneSnapshot<ProfileInterest>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(ProfileInterest::class.java).equalTo("email", sp.fetchEmailId()),
            CloudDbConstant.POLICY
        )
    }

    /**
     * Query to get the last primary key inserted into interest table of cloud DB
     *
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    fun getMaxInterestIdQuery(mCloudDBZone: CloudDBZone): Task<CloudDBZoneSnapshot<ProfileInterest>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(ProfileInterest::class.java).orderByDesc("id").limit(1),
            CloudDbConstant.POLICY
        )
    }

    /**
     * Query to get the last primary key inserted into interest table of cloud DB
     *
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    fun getMaxBookmarkIdQuery(mCloudDBZone: CloudDBZone): Task<CloudDBZoneSnapshot<Bookmark>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(Bookmark::class.java)
                .orderByDesc("bookmarkid").limit(1), CloudDbConstant.POLICY
        )
    }

    /**
     * Query to fetch particular row of bookmark they we want to delete
     *
     * @param mCloudDBZone cloud db object
     * @param email of the user
     * @param page number to be deleted
     * @param bookid book id of the book
     * @return cloud db query result
     */
    fun getDelQuery(mCloudDBZone: CloudDBZone, email: String?, page: Int, bookid: Int): Task<CloudDBZoneSnapshot<Bookmark>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(Bookmark::class.java)
                .equalTo("emailid", email).equalTo("pageno", page)
                .equalTo("bookId", bookid), CloudDbConstant.POLICY
        )
    }

    /**
     * Query to get the all the books stored into cloud
     *
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    fun getBookResultQuery(mCloudDBZone: CloudDBZone): Task<CloudDBZoneSnapshot<Books>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(Books::class.java),
            CloudDbConstant.POLICY
        )
    }

    /**
     * Query to search a book
     *
     * @param query to search a book
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    fun getSearchResults(
        query: String?,
        mCloudDBZone: CloudDBZone
    ): Task<CloudDBZoneSnapshot<Books>> {
        return mCloudDBZone.executeQuery(
            CloudDBZoneQuery.where(Books::class.java).contains("bookName", query!!),
            CloudDbConstant.POLICY
        )
    }

    companion object {
        var cloudDb: CloudDbQueries? = null

        /**
         * Initializes Cloud Db
         *
         * @return Cloud DB instance
         */
        @get:Synchronized
        val instance: CloudDbQueries?
            get() {
                if (cloudDb == null) {
                    cloudDb = CloudDbQueries()
                }
                return cloudDb
            }
    }
}