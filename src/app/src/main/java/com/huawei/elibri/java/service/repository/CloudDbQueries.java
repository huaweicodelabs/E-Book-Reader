/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.service.repository;

import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.service.model.Bookmark;
import com.huawei.elibri.java.service.model.Books;
import com.huawei.elibri.java.service.model.Profile;
import com.huawei.elibri.java.service.model.ProfileInterest;
import com.huawei.hmf.tasks.Task;

/**
 * @author: nWX914751
 * @since: 26-11-2020
 */
public class CloudDbQueries {
    static CloudDbQueries cloudDb;

    /**
     * Initializes Cloud Db
     *
     * @return Cloud DB instance
     */
    public static synchronized CloudDbQueries getInstance() {
        if (cloudDb == null) {
            cloudDb = new CloudDbQueries();
        }
        return cloudDb;
    }

    /**
     * Query to get the user data from cloud db table
     *
     * @param sp is shared preference instance
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    public Task<CloudDBZoneSnapshot<Profile>> getUserProfile(SavedPreference sp, CloudDBZone mCloudDBZone) {
        return mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Profile.class).equalTo("emailid", sp.fetchEmailId()), CloudDbConstant.POLICY);
    }

    /**
     * Query for user interests
     *
     * @param sp is shared preference instance
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    public Task<CloudDBZoneSnapshot<ProfileInterest>> getUserInterestsQuery(
            SavedPreference sp, CloudDBZone mCloudDBZone) {
        return mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(ProfileInterest.class).equalTo("email", sp.fetchEmailId()),
                CloudDbConstant.POLICY);
    }

    /**
     * Query to get the last primary key inserted into interest table of cloud DB
     *
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    public Task<CloudDBZoneSnapshot<ProfileInterest>> getMaxInterestIdQuery(CloudDBZone mCloudDBZone) {
        return mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(ProfileInterest.class).orderByDesc("id").limit(1), CloudDbConstant.POLICY);
    }

    /**
     * Query to get the last primary key inserted into interest table of cloud DB
     *
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    public Task<CloudDBZoneSnapshot<Bookmark>> getMaxBookmarkIdQuery(CloudDBZone mCloudDBZone) {
        return mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Bookmark.class).orderByDesc("bookmarkid").limit(1), CloudDbConstant.POLICY);
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
    public Task<CloudDBZoneSnapshot<Bookmark>> getDelQuery(CloudDBZone mCloudDBZone,
        String email, int page, int bookid) {
        return mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Bookmark.class).equalTo("emailid", email).equalTo("pageno", page)
                        .equalTo("bookId", bookid), CloudDbConstant.POLICY);
    }

    /**
     * Query to get the all the books stored into cloud
     *
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    public Task<CloudDBZoneSnapshot<Books>> getBookResultQuery(CloudDBZone mCloudDBZone) {
        return mCloudDBZone.executeQuery(CloudDBZoneQuery.where(Books.class), CloudDbConstant.POLICY);
    }

    /**
     * Query to search a book
     *
     * @param query to search a book
     * @param mCloudDBZone cloud db object
     * @return cloud db query result
     */
    public Task<CloudDBZoneSnapshot<Books>> getSearchResults(String query, CloudDBZone mCloudDBZone) {
        return mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Books.class).contains("bookName", query), CloudDbConstant.POLICY);
    }
}
