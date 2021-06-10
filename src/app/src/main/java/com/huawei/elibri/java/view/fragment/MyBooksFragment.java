/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.service.model.Books;
import com.huawei.elibri.java.service.repository.CloudDbQueries;
import com.huawei.elibri.java.utility.Util;
import com.huawei.elibri.java.view.activities.MainActivity;
import com.huawei.elibri.java.adapter.CustomAdapter;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;

import java.util.List;

/**
 * This class is used to display all the books with book name and book type.
 *
 * @author lWX916345
 * @since 03-11-2020
 */
public class MyBooksFragment extends BookBaseFragment {
    private static final String TAG = MyBooksFragment.class.getSimpleName();

    private CloudDBZone mCloudDBZone;
    private List<Books> bookList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.search);
        if (item != null) {
            menu.removeItem(R.id.search);
        }
        inflater.inflate(R.menu.menu_mybooks, menu);
        item = menu.findItem(R.id.search);
        initializeSearch(item);
    }

    /**
     * Initialize search
     *
     * @param item MenuItem
     */
    private void initializeSearch(MenuItem item) {
        SearchView sv = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        item.setActionView(sv);
        sv.setIconifiedByDefault(true);
        sv.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        fetchBookList(getView(), query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
        sv.setOnCloseListener(
                () -> {
                    sv.onActionViewCollapsed();
                    fetchBookList(getView(), "");
                    return false;
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_all_books, container, false);
        FrameLayout mFrameLayout = view.findViewById(R.id.my_books_fragment_fl);
        mFrameLayout.setBackgroundResource(R.color.white);
        BannerView bottomBannerView = view.findViewById(R.id.hw_banner_view);
        if (!SavedPreference.getInstance(getContext().getApplicationContext()).getIsSubscribed()) {
            showBannerAds(bottomBannerView);
        } else {
            bottomBannerView.setVisibility(View.GONE);
        }
        initializeUi(view);
        return view;
    }

    private void showBannerAds(BannerView view) {
        HwAds.init(getContext());
        AdParam adParam = new AdParam.Builder().build();
        view.loadAd(adParam);
    }

    /**
     * Initializes UI
     *
     * @param view RootView
     */
    private void initializeUi(View view) {
        Util.setProgressBar(getContext());
        initializeDb(
                new DBOpenInterface() {
                    @Override
                    public void success(CloudDBZone cloudDBZone) {
                        Util.stopProgressBar();
                        mCloudDBZone = cloudDBZone;
                        fetchBookList(view, "");
                    }

                    @Override
                    public void failure() {
                        Util.stopProgressBar();
                    }
                });
    }

    /**
     * fetch book list
     *
     * @param view  rootView
     * @param query search keyword
     */
    private void fetchBookList(View view, String query) {
        Util.setProgressBar(getContext());

        if (mCloudDBZone == null) {
            return;
        }
        Task<CloudDBZoneSnapshot<Books>> queryTask;
        if (!TextUtils.isEmpty(query)) {
            queryTask = CloudDbQueries.getInstance().getSearchResults(query, mCloudDBZone);
        } else {
            queryTask = CloudDbQueries.getInstance().getBookResultQuery(mCloudDBZone);
        }
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            Util.stopProgressBar();
                            Log.d(TAG,"Book list fetched successfully");
                            bookList = processBookQueryResult(snapshot);
                            RecyclerView recyclerView = view.findViewById(R.id.rvMyBooks);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
                            recyclerView.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
                            CustomAdapter customAdapter = new CustomAdapter(MyBooksFragment.this, bookList);
                            recyclerView.setAdapter(customAdapter); // set the Adapter to RecyclerView
                        })
                .addOnFailureListener(
                        exception -> {
                            Util.stopProgressBar();
                            showToast(exception.getLocalizedMessage(), getContext());
                        });
    }

    /**
     * Saves DB result in list
     *
     * @param snapshot query result
     * @return book list
     */
    private List<Books> processBookQueryResult(CloudDBZoneSnapshot<Books> snapshot) {
        return getBooks(snapshot);
    }

    @Override
    public void onResume() {
        super.onResume();
        showAlertExitDialog(getView());
    }
}
