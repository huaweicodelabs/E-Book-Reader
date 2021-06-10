/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.view.fragment

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.interfaces.DownloadPdf
import com.huawei.elibri.kotlin.utility.DownloadTask
import com.huawei.elibri.kotlin.utility.Util.getSdkDirPath
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import java.io.File
import java.io.IOException

/**
 * This class is used to display bookmarked page.
 *
 * @since 25-10-2020
 * @author lWX916345
 */
class DisplayBookmark : BaseFragment() {
    private val TAG = DisplayBookmark::class.java.name
    private var pdfView: ImageView? = null
    private var mPageNo = 0
    private var mBookUrl: String? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_display_bookmark, container, false)
        pdfView =
            view.findViewById<View>(R.id.imgBookmarkPdfView) as ImageView
        val animation1 =
            AnimationUtils.loadAnimation(activity, R.anim.zoomin)
        pdfView!!.startAnimation(animation1)
        val mDisplayBookmarkFl =
            view.findViewById<FrameLayout>(R.id.display_bookmark_fl)
        mDisplayBookmarkFl.setBackgroundResource(R.color.white)
        val bundle = this.arguments
        if (bundle != null) {
            mPageNo = bundle.getInt("PAGE_NO", 101)
            mBookUrl = bundle.getString("PAGE_URL", "")
        }
        try {
            displayBook()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
        return view
    }

    /**
     * Open the pdf file either from internal storage or download it from internet
     *
     * @throws IOException while getting the file path
     */
    @Throws(IOException::class)
    private fun displayBook() {
        setProgressBar(context)
        val fileName =
            mBookUrl!!.substring(mBookUrl!!.lastIndexOf('/') + 1, mBookUrl!!.length)
        val file =
            File(getSdkDirPath(activity!!), fileName)
        if (file.exists()) {
            stopProgressBar()
            showPdfFromFile(file)
        } else {
            downloadPdfFromInternet(mBookUrl)
        }
    }

    /**
     * Download the pdf file from URL
     *
     * @param url of pdf file
     * @throws IOException exception
     */
    @Throws(IOException::class)
    private fun downloadPdfFromInternet(url: String?) {
        DownloadTask(activity!!, url!!, object : DownloadPdf {
            override fun onPdfDownloaded(file: File?) {
                stopProgressBar()
                showPdfFromFile(file)
            }

            override fun onError(error: String?) {
                stopProgressBar()
                showToast(error, activity)
            }
        })
    }

    /**
     * Render thePDF file
     *
     * @param file name that we want to render
     */
    private fun showPdfFromFile(file: File?) {
        try {
            parcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            if (parcelFileDescriptor != null) {
                pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
                showPage(mPageNo)
            }
        } catch (exception: IOException) {
            Log.e(TAG, "IOException")
        }
    }

    /**
     * Display the PDF page
     *
     * @param index is the page number of pdf file that we want to display
     */
    private fun showPage(index: Int) {
        if (pdfRenderer!!.pageCount <= index) {
            return
        }
        if (currentPage != null) {
            currentPage!!.close()
        }
        currentPage = pdfRenderer!!.openPage(index)

    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
    }
}