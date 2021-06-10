/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.Navigation
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.adapter.CustomExpandableListAdapter
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.service.model.Bookmark
import com.huawei.elibri.kotlin.service.model.Books
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.hmf.tasks.Task
import java.util.*

/**
 * This class is used to display all the bookmarks along with their book name.
 *
 * @author lWX916345
 * @since: 13-11-2020
 */
class BookmarkFragment : BookBaseFragment() {
    private var expandableListView: ExpandableListView? = null
    private var expandableListTitle: List<String?>? = null
    private var tvNoBookmark: TextView? = null
    private var expandableListDetail: HashMap<String?, List<Int>>? =
        null
    private var bookUrls: HashMap<String, String>? = null
    private var mCloudDBZone: CloudDBZone? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)
        expandableListView = view.findViewById(R.id.exlvBookmarks)
        tvNoBookmark = view.findViewById(R.id.tvNoBookmark)
        expandableListDetail = HashMap()
        val mBookmarkFragment =
            view.findViewById<FrameLayout>(R.id.bookmark_fragment_fl)
        mBookmarkFragment.setBackgroundResource(R.color.white)
        initializeDb()
        return view
    }

    /**
     * Initialize cloud database
     */
    private fun initializeDb() {
        setProgressBar(context)
        initializeDb(
            object : DBOpenInterface {
                override fun success(cloudDBZone: CloudDBZone?) {
                    stopProgressBar()
                    mCloudDBZone = cloudDBZone
                    fetchBookList()
                    queryBookmarkList()
                }

                override fun failure() {
                    stopProgressBar()
                    setAdapter(true)
                }
            })
    }

    /**
     * Set the adapter to display all the bookmarks
     *
     * @param isBookmarkListEmpty to check user have anybookmark or not
     */
    private fun setAdapter(isBookmarkListEmpty: Boolean) {
        if (isBookmarkListEmpty) {
            tvNoBookmark!!.visibility = View.VISIBLE
            expandableListView!!.visibility = View.GONE
        } else {
            tvNoBookmark!!.visibility = View.GONE
            expandableListView!!.visibility = View.VISIBLE
            expandableListTitle = ArrayList(expandableListDetail!!.keys)
            val expandableListAdapter = CustomExpandableListAdapter(activity!!, expandableListTitle as List<String>, expandableListDetail
            )
            expandableListView!!.setAdapter(expandableListAdapter)
            expandableListView!!.setOnGroupExpandListener { groupPosition: Int ->
                Log.d(
                    "$TAG book url is:",
                    bookUrls!!.get((expandableListTitle as ArrayList<String?>).get(groupPosition))!!
                )
            }
            expandableListView!!.setOnGroupCollapseListener { groupPosition: Int -> }
            expandableListView!!.setOnChildClickListener { parent: ExpandableListView?, v1: View?, groupPosition: Int, childPosition: Int, id: Long ->
                val bundle = Bundle()
                bundle.putInt(
                    "PAGE_NO",
                    expandableListDetail!![(expandableListTitle as ArrayList<String?>).get(groupPosition)]!![childPosition]
                )
                bundle.putString("PAGE_URL", bookUrls!![(expandableListTitle as ArrayList<String?>).get(groupPosition)])
                Navigation.findNavController(this.view!!)
                    .navigate(R.id.action_nav_bookmark_to_display_bookmark, bundle)
                false
            }
        }
    }

    /**
     * Fetch the data from bookmark table of cloud db
     *
     * @param snapshot of the bookmark table
     */
    private fun processQueryResult(snapshot: CloudDBZoneSnapshot<Bookmark>) {
        var isBookmarkListEmpty = true
        var bookmarkInfoList: ArrayList<Bookmark> = ArrayList()
        bookmarkInfoList = getBookmarks(snapshot, bookmarkInfoList)
        if (bookmarkInfoList.size == 0) {
            isBookmarkListEmpty = true
        } else {
            isBookmarkListEmpty = false
            Log.d(
                "db",
                "processQueryResult success list$bookmarkInfoList"
            )
            val bookIds: MutableList<Int> = ArrayList()
            for (i in bookmarkInfoList.indices) {
                if (bookIds.contains(bookmarkInfoList[i].bookId)) {
                    continue
                } else {
                    bookIds.add(bookmarkInfoList[i].bookId)
                }
            }
            var bookname: String? = ""
            for (i in bookIds.indices) {
                val bookDetail: MutableList<Int> = ArrayList()
                for (j in bookmarkInfoList.indices) {
                    if (bookmarkInfoList[j].bookId == bookIds[i]) {
                        bookname = bookmarkInfoList[j].bookname
                        bookDetail.add(bookmarkInfoList[j].pageno + 1)
                        Log.d(
                            TAG + "bookmarkInfoList ",
                            bookmarkInfoList[j].pageno.toString()
                        )
                    }
                }
                expandableListDetail!![bookname] = bookDetail
            }
        }
        setAdapter(isBookmarkListEmpty)
    }

    /**
     * query for all the bookmarks from cloud db
     */
    fun queryBookmarkList() {
        if (mCloudDBZone == null) {
            return
        }
        val queryTask =
            mCloudDBZone!!.executeQuery(
                CloudDBZoneQuery.where(Bookmark::class.java)
                    .equalTo(
                        "emailid",
                        SavedPreference.getInstance(context!!)?.fetchEmailId()
                    ), CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
        queryTask.addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Bookmark> ->
            processQueryResult(snapshot)
        }.addOnFailureListener { obj: Exception -> obj.message }
    }

    /**
     * It will ask the user does he wants to exit the app
     */
    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!
            .setOnKeyListener(
                View.OnKeyListener setOnKeyListener@{ view: View?, i: Int, keyEvent: KeyEvent ->
                    if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                        showDialog(context)
                        return@setOnKeyListener true
                    }
                    false
                }
            )
    }

    /**
     * Query all the books list from cloud db
     */
    private fun fetchBookList() {
        if (mCloudDBZone == null) {
            return
        }
        val queryTask: Task<CloudDBZoneSnapshot<Books>>
        queryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(Books::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Books> ->
                processBookQueryResult(
                    snapshot
                )
            }
            .addOnFailureListener { exception: Exception ->
                Log.d(
                    TAG,
                    exception.localizedMessage
                )
                setAdapter(true)
                showToast(exception.localizedMessage, context)
            }
    }

    /**
     * Process the book id which we get from the book table of cloud db
     *
     * @param snapshot of Books table
     */
    private fun processBookQueryResult(snapshot: CloudDBZoneSnapshot<Books>) {
        bookUrls = HashMap()
        val bookInfoList: List<Books?> = getBooks(snapshot)
        for (i in bookInfoList.indices) {
            bookUrls!![bookInfoList[i]!!.bookName] = bookInfoList[i]!!.bookUrl
        }
    }

    companion object {
        private val TAG = BookmarkFragment::class.java.simpleName
    }
}