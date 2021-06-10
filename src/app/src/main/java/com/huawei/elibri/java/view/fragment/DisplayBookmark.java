/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.fragment;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.elibri.R;
import com.huawei.elibri.java.interfaces.DownloadPdf;
import com.huawei.elibri.java.utility.DownloadTask;
import com.huawei.elibri.java.utility.Util;

import java.io.File;
import java.io.IOException;

/**
 * This class is used to display bookmarked page.
 *
 * @since 25-10-2020
 * @author lWX916345
 */
public class DisplayBookmark extends BaseFragment {
    private final String TAG = DisplayBookmark.class.getName();
    private ImageView pdfView;
    private int mPageNo;
    private String mBookUrl;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_bookmark, container, false);
        pdfView = (ImageView) view.findViewById(R.id.imgBookmarkPdfView);
        Animation animation1 = AnimationUtils.loadAnimation(getActivity(), R.anim.zoomin);
        pdfView.startAnimation(animation1);
        FrameLayout mDisplayBookmarkFl = view.findViewById(R.id.display_bookmark_fl);
        mDisplayBookmarkFl.setBackgroundResource(R.color.white);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mPageNo = bundle.getInt("PAGE_NO", 101);
            mBookUrl = bundle.getString("PAGE_URL", "");
        }
        try {
            displayBook();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return view;
    }

    /**
     * Open the pdf file either from internal storage or download it from internet
     *
     * @throws IOException while getting the file path
     */
    private void displayBook() throws IOException {
        Util.setProgressBar(getContext());
        String fileName = mBookUrl.substring(mBookUrl.lastIndexOf('/') + 1, mBookUrl.length());
        File file = new File(Util.getSdkDirPath(getActivity()), fileName);
        if (file.exists()) {
            Util.stopProgressBar();
            showPdfFromFile(file);
        } else {
            downloadPdfFromInternet(mBookUrl);
        }
    }

    /**
     * Download the pdf file from URL
     *
     * @param url of pdf file
     * @throws IOException exception
     */
    private void downloadPdfFromInternet(String url) throws IOException {
        new DownloadTask(getActivity(), url, new DownloadPdf() {
            @Override
            public void onPdfDownloaded(File file) {
                Util.stopProgressBar();
                showPdfFromFile(file);
            }

            @Override
            public void onError(String error) {
                Util.stopProgressBar();
                showToast(error,getActivity());
            }
        });
    }

    /**
     * Render thePDF file
     *
     * @param file name that we want to render
     */
    private void showPdfFromFile(File file) {
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            if (parcelFileDescriptor != null) {
                pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                showPage(mPageNo);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException");
        }
    }

    /**
     * Display the PDF page
     *
     * @param index is the page number of pdf file that we want to display
     */
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        if (currentPage != null) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pdfView.setImageBitmap(bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
    }

}