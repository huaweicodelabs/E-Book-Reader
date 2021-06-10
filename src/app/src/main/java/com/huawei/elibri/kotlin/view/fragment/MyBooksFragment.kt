/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.adapter.CustomAdapter
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.service.model.Books
import com.huawei.elibri.kotlin.service.repository.CloudDbQueries
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.elibri.kotlin.view.activities.MainActivity
import com.huawei.hmf.tasks.Task
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView

/**
 * This class is used to display all the books with book name and book type.
 *
 * @author lWX916345
 * @since 03-11-2020
 */
class MyBooksFragment : BookBaseFragment() {
    private var mCloudDBZone: CloudDBZone? = null
    private var bookList: List<Books>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        var item = menu.findItem(R.id.search)
        if (item != null) {
            menu.removeItem(R.id.search)
        }
        inflater.inflate(R.menu.menu_mybooks, menu)
        item = menu.findItem(R.id.search)
        initializeSearch(item)
    }

    /**
     * Initialize search
     *
     * @param item MenuItem
     */
    private fun initializeSearch(item: MenuItem?) {
        val sv = SearchView(
            (activity as MainActivity?)!!.supportActionBar!!.themedContext
        )
        item!!.actionView = sv
        sv.setIconifiedByDefault(true)
        sv.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    fetchBookList(view, query)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        sv.setOnCloseListener {
            sv.onActionViewCollapsed()
            fetchBookList(view, "")
            false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frag_all_books, container, false)
        val mFrameLayout = view.findViewById<FrameLayout>(R.id.my_books_fragment_fl)
        mFrameLayout.setBackgroundResource(R.color.white)
        val bottomBannerView: BannerView = view.findViewById(R.id.hw_banner_view)
        if (SavedPreference.getInstance(context!!.applicationContext)?.isSubscribed!!) {
            showBannerAds(bottomBannerView)
        } else {
            bottomBannerView.visibility = View.GONE
        }
        initializeUi(view)
        return view
    }

    private fun showBannerAds(view: BannerView) {
        HwAds.init(context)
        val adParam = AdParam.Builder().build()
        view.loadAd(adParam)
    }

    /**
     * Initializes UI
     *
     * @param view RootView
     */
    private fun initializeUi(view: View) {
        setProgressBar(context)
        initializeDb(
            object : DBOpenInterface {
                override fun success(cloudDBZone: CloudDBZone?) {
                    stopProgressBar()
                    mCloudDBZone = cloudDBZone
                    fetchBookList(view, "")
                }

                override fun failure() {
                    stopProgressBar()
                }
            })
    }

    /**
     * fetch book list
     *
     * @param view  rootView
     * @param query search keyword
     */
    private fun fetchBookList(view: View?, query: String) {
        setProgressBar(context)
        if (mCloudDBZone == null) {
            return
        }
        val queryTask: Task<CloudDBZoneSnapshot<Books>>
        queryTask = if (!TextUtils.isEmpty(query)) {
            CloudDbQueries.instance!!.getSearchResults(query, mCloudDBZone!!)
        } else {
            CloudDbQueries.instance!!.getBookResultQuery(mCloudDBZone!!)
        }
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Books> ->
                stopProgressBar()
                Log.d(
                    TAG,
                    "Book list fetched successfully"
                )
                bookList = processBookQueryResult(snapshot) as List<Books>?
                val recyclerView: RecyclerView = view!!.findViewById(R.id.rvMyBooks)
                val gridLayoutManager = GridLayoutManager(activity, 2)
                recyclerView.layoutManager = gridLayoutManager // set LayoutManager to RecyclerView
                val customAdapter = CustomAdapter(this@MyBooksFragment, bookList!!)
                recyclerView.adapter = customAdapter // set the Adapter to RecyclerView
            }
            .addOnFailureListener { exception: Exception ->
                stopProgressBar()
                showToast(exception.localizedMessage, context)
            }
    }

    /**
     * Saves DB result in list
     *
     * @param snapshot query result
     * @return book list
     */
    private fun processBookQueryResult(snapshot: CloudDBZoneSnapshot<Books>): List<Books?>? {
        return getBooks(snapshot)
    }

    override fun onResume() {
        super.onResume()
        showAlertExitDialog(view!!)
    }

    companion object {
        private val TAG = MyBooksFragment::class.java.simpleName
    }
}