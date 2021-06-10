/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.util.Log;

import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.elibri.java.service.model.Bookmark;
import com.huawei.elibri.java.service.model.Books;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: nWX914751
 * @since: 17-12-2020
 */
public class BookBaseFragment extends BaseFragment {
    private static final String TAG = BookBaseFragment.class.getSimpleName();

    public List<Bookmark> getBookmarks(CloudDBZoneSnapshot<Bookmark> snapshot, List<Bookmark> bookmarkInfoList) {
        CloudDBZoneObjectList<Bookmark> bookmarkInfoCursor = snapshot.getSnapshotObjects();
        try {
            while (bookmarkInfoCursor.hasNext()) {
                bookmarkInfoList.add(bookmarkInfoCursor.next());
            }
        } catch (AGConnectCloudDBException e) {
            Log.d(TAG, "AGConnectCloudDBException processQuer catch");
            Log.w(TAG, e);
        }
        snapshot.release();
        return bookmarkInfoList;
    }

    /**
     * Fetch books
     *
     * @param snapshot result
     * @return list of books
     */
    public List<Books> getBooks(CloudDBZoneSnapshot<Books> snapshot) {
        CloudDBZoneObjectList<Books> bookInfoCursor = snapshot.getSnapshotObjects();
        List<Books> bookInfoList = new ArrayList<>(10);
        try {
            while (bookInfoCursor.hasNext()) {
                Books bookData = bookInfoCursor.next();
                bookInfoList.add(bookData);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, e);
        } finally {
            snapshot.release();
        }
        return bookInfoList;
    }
}
