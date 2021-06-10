/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.navigation.Navigation;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.adapter.CustomExpandableListAdapter;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.service.model.Bookmark;
import com.huawei.elibri.java.service.model.Books;
import com.huawei.elibri.java.utility.Util;
import com.huawei.hmf.tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to display all the bookmarks along with their book name.
 *
 * @author lWX916345
 * @since: 13-11-2020
 */
public class BookmarkFragment extends BookBaseFragment {
    private static final String TAG = BookmarkFragment.class.getSimpleName();
    private ExpandableListView expandableListView;
    private List<String> expandableListTitle;
    private TextView tvNoBookmark = null;
    private HashMap<String, List<Integer>> expandableListDetail = null;
    private HashMap<String, String> bookUrls;
    private CloudDBZone mCloudDBZone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        expandableListView = view.findViewById(R.id.exlvBookmarks);
        tvNoBookmark = view.findViewById(R.id.tvNoBookmark);
        expandableListDetail = new HashMap<>();
        FrameLayout mBookmarkFragment = view.findViewById(R.id.bookmark_fragment_fl);
        mBookmarkFragment.setBackgroundResource(R.color.white);
        initializeDb();
        return view;
    }

    /**
     * Initialize cloud database
     */
    private void initializeDb() {
        Util.setProgressBar(getContext());
        initializeDb(
                new DBOpenInterface() {
                    @Override
                    public void success(CloudDBZone cloudDBZone) {
                        Util.stopProgressBar();
                        mCloudDBZone = cloudDBZone;
                        fetchBookList();
                        queryBookmarkList();
                    }

                    @Override
                    public void failure() {
                        Util.stopProgressBar();
                        setAdapter(true);
                    }
                });
    }


    /**
     * Set the adapter to display all the bookmarks
     *
     * @param isBookmarkListEmpty to check user have anybookmark or not
     */
    private void setAdapter(boolean isBookmarkListEmpty) {
        if (isBookmarkListEmpty) {
            tvNoBookmark.setVisibility(View.VISIBLE);
            expandableListView.setVisibility(View.GONE);
        } else {
            tvNoBookmark.setVisibility(View.GONE);
            expandableListView.setVisibility(View.VISIBLE);
            expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
            CustomExpandableListAdapter expandableListAdapter =
                    new CustomExpandableListAdapter(getActivity(), expandableListTitle, expandableListDetail);
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.setOnGroupExpandListener(groupPosition -> {
                Log.d(TAG + " book url is:", bookUrls.get(expandableListTitle.get(groupPosition)));
            });
            expandableListView.setOnGroupCollapseListener(groupPosition -> {
            });
            expandableListView.setOnChildClickListener((parent, v1, groupPosition, childPosition, id) -> {
                Bundle bundle = new Bundle();
                bundle.putInt("PAGE_NO", expandableListDetail.get(expandableListTitle.get(groupPosition)).get(
                        childPosition));
                bundle.putString("PAGE_URL", bookUrls.get(expandableListTitle.get(groupPosition)));
                Navigation.findNavController(this.getView())
                        .navigate(R.id.action_nav_bookmark_to_display_bookmark, bundle);
                return false;
            });
        }
    }

    /**
     * Fetch the data from bookmark table of cloud db
     *
     * @param snapshot of the bookmark table
     */
    private void processQueryResult(CloudDBZoneSnapshot<Bookmark> snapshot) {
        boolean isBookmarkListEmpty = true;
        List<Bookmark> bookmarkInfoList = new ArrayList<>();
        bookmarkInfoList = getBookmarks(snapshot, bookmarkInfoList);
        if (bookmarkInfoList.size() == 0) {
            isBookmarkListEmpty = true;
        }else {
            isBookmarkListEmpty = false;
            Log.d("db", "processQueryResult success list" + bookmarkInfoList.toString());
            List<Integer> bookIds = new ArrayList<Integer>();
            for (int i = 0; i < bookmarkInfoList.size(); i++) {
                if (bookIds.contains(bookmarkInfoList.get(i).getBookId())) {
                    continue;
                } else {
                    bookIds.add(bookmarkInfoList.get(i).getBookId());
                }
            }
            String bookname = "";
            for (int i = 0; i < bookIds.size(); i++) {
                List<Integer> bookDetail = new ArrayList<>();
                for (int j = 0; j < bookmarkInfoList.size(); j++) {
                    if (bookmarkInfoList.get(j).getBookId().equals(bookIds.get(i))) {
                        bookname = bookmarkInfoList.get(j).getBookname();
                        bookDetail.add(bookmarkInfoList.get(j).getPageno() + 1);
                        Log.d(TAG + "bookmarkInfoList ", bookmarkInfoList.get(j).getPageno().toString());
                    }
                }
                expandableListDetail.put(bookname, bookDetail);
            }
        }
        setAdapter(isBookmarkListEmpty);
    }

    /**
     * query for all the bookmarks from cloud db
     */
    public void queryBookmarkList() {
        if (mCloudDBZone == null) {
            return;
        }
        Task<CloudDBZoneSnapshot<Bookmark>> queryTask =
                mCloudDBZone.executeQuery(
                        CloudDBZoneQuery.where(Bookmark.class).equalTo("emailid",
                        SavedPreference.getInstance(getContext())
                        .fetchEmailId()), CloudDBZoneQuery
                        .CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processQueryResult).addOnFailureListener(Throwable::getMessage);
    }

    /**
     * It will ask the user does he wants to exit the app
     */
    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView()
                .setOnKeyListener(
                        (view, i, keyEvent) -> {
                            if (keyEvent.getAction() == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                                showDialog(getContext());
                                return true;
                            }
                            return false;
                        });
    }

    /**
     * Query all the books list from cloud db
     */
    private void fetchBookList() {
        if (mCloudDBZone == null) {
            return;
        }

        Task<CloudDBZoneSnapshot<Books>> queryTask;
        queryTask =
                mCloudDBZone.executeQuery(
                        CloudDBZoneQuery.where(Books.class),
                        CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);

        queryTask
                .addOnSuccessListener(
                        snapshot -> processBookQueryResult(snapshot))
                .addOnFailureListener(
                        exception -> {
                            Log.d(TAG, exception.getLocalizedMessage());
                            setAdapter(true);
                            showToast(exception.getLocalizedMessage(), getContext());
                        });
    }

    /**
     * Process the book id which we get from the book table of cloud db
     *
     * @param snapshot of Books table
     */
    private void processBookQueryResult(CloudDBZoneSnapshot<Books> snapshot) {
        bookUrls = new HashMap<>();
        List<Books> bookInfoList = getBooks(snapshot);
        for (int i = 0; i < bookInfoList.size(); i++) {
            bookUrls.put(bookInfoList.get(i).getBookName(), bookInfoList.get(i).getBookUrl());
        }
    }


}
