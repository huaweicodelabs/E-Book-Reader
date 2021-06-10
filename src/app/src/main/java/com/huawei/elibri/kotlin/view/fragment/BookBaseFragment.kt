/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.util.Log
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.elibri.kotlin.service.model.Bookmark
import com.huawei.elibri.kotlin.service.model.Books
import java.util.*

/**
 * @author: nWX914751
 * @since: 17-12-2020
 */
open class BookBaseFragment : BaseFragment() {
    fun getBookmarks(
        snapshot: CloudDBZoneSnapshot<Bookmark>?,
        bookmarkInfoList: ArrayList<Bookmark>
    ): ArrayList<Bookmark> {
        val bookmarkInfoCursor =
            snapshot!!.snapshotObjects
        try {
            while (bookmarkInfoCursor.hasNext()) {
                bookmarkInfoList.add(bookmarkInfoCursor.next())
            }
        } catch (e: AGConnectCloudDBException) {
            Log.d(
                TAG,
                "AGConnectCloudDBException processQuer catch"
            )
            Log.w(TAG, e)
        }
        snapshot.release()
        return bookmarkInfoList
    }

    /**
     * Fetch books
     *
     * @param snapshot result
     * @return list of books
     */
    fun getBooks(snapshot: CloudDBZoneSnapshot<Books>): MutableList<Books?> {
        val bookInfoCursor = snapshot.snapshotObjects
        val bookInfoList: MutableList<Books?> = ArrayList(10)
        try {
            while (bookInfoCursor.hasNext()) {
                val bookData = bookInfoCursor.next()
                bookInfoList.add(bookData)
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, e)
        } finally {
            snapshot.release()
        }
        return bookInfoList
    }

    companion object {
        private val TAG = BookBaseFragment::class.java.simpleName
    }
}