/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 * Copyright 2017 Bartosz Schiller
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
* GNU General Lesser Public License (LGPL) version 3.0 - http://www.gnu.org/licenses/lgpl.html
* Mozilla Public License Version 2.0 - http://www.mozilla.org/MPL/2.0/
*
 */
package com.huawei.elibri.kotlin.view.fragment

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.Message
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.Pair
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.SavedPreference
import com.huawei.elibri.kotlin.interfaces.DBOpenInterface
import com.huawei.elibri.kotlin.interfaces.DownloadPdf
import com.huawei.elibri.kotlin.service.model.Bookmark
import com.huawei.elibri.kotlin.service.repository.CloudDbQueries
import com.huawei.elibri.kotlin.utility.DownloadTask
import com.huawei.elibri.kotlin.utility.Util.getSdkDirPath
import com.huawei.elibri.kotlin.utility.Util.setProgressBar
import com.huawei.elibri.kotlin.utility.Util.stopProgressBar
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator
import com.huawei.hms.mlsdk.tts.*
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Open the book for reading with TTs functionality
 *
 * @author lWX916345
 * @since 25-10-2020
 */
class DisplayBook : BookBaseFragment(), View.OnClickListener {
    private var bookmarkMaxId = 0
    private var btnBookmarkPage: Button? = null
    private var btnNext: Button? = null
    private var btnPrev: Button? = null
    private var imgPdfView: ImageView? = null
    private var downloadedFilePath: File? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var mBookUrl: String? = null
    private var mBookName: String? = null
    private var isPause = false
    private val speedVal = 1.0f
    private val volumeVal = 1.0f
    private var mlTtsEngine: MLTtsEngine? = null
    private var mlConfigs: MLTtsConfig? = null
    private val defaultLanguageCode = "en-US"
    private val defaultSpeakerCode = "en-US-st-1"
    private var mPlay: Button? = null
    private var mStop: Button? = null
    private var mRepeat: Button? = null
    private var translator: MLRemoteTranslator? = null
    private var mCloudDBZone: CloudDBZone? = null
    private var sp: SavedPreference? = null
    private var mCurrentBookId = 0
    private var mCurrentBookDetails: MutableList<Int?>? = null
    private var mAudioBtnLl: LinearLayout? = null
    private var rlPrevNext: RelativeLayout? = null
    private var mPdfExtractedText = ""
    private var russian_btn: TextView? = null
    private var hindi_btn: TextView? = null
    private var chinese_btn: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_display_book, container, false)
        initView(view)
        sp = SavedPreference.getInstance(context!!)
        val bundle = this.arguments
        if (bundle != null) {
            mCurrentBookId = bundle.getInt("BOOK_ID", 101)
            mBookUrl = bundle.getString("BOOK_URL", "")
            mBookName = bundle.getString("BOOK_NAME", "")
        }
        openCloudDBZone()
        btnBookmarkPage!!.setOnClickListener { bookmarkPdfPage() }
        return view
    }

    /**
     * To set up TTS configuration
     */
    private fun updateConfig() {
        val mlTtsConfig = MLTtsConfig()
            .setVolume(volumeVal)
            .setSpeed(speedVal)
            .setLanguage(defaultLanguageCode)
            .setPerson(defaultSpeakerCode)

        mlTtsEngine = MLTtsEngine(mlConfigs)
        mlTtsEngine!!.updateConfig(mlTtsConfig)
    }

    /**
     * Translate the content using ML kit
     *
     * @param lang in which we want to translate the content
     */
    private fun remoteTranslator(lang: String) {
        setProgressBar(context)
        // Create an analyzer. You can customize the analyzer by creating MLRemoteTranslateSetting
        val setting =
            MLRemoteTranslateSetting.Factory().setTargetLangCode(lang).create()
        translator = MLTranslatorFactory.getInstance().getRemoteTranslator(setting)
        val sourceText = readPdf(downloadedFilePath!!.path)
        if (sourceText != null) {
            val task = translator?.asyncTranslate(sourceText)
            task?.addOnSuccessListener { text ->
                stopProgressBar()
                remoteDisplaySuccess(text, true)
                if (lang === "hi") {
                    hindi_btn!!.setTextColor(resources.getColor(R.color.white))
                    chinese_btn!!.setBackgroundResource(R.drawable.textview_border_style)
                    hindi_btn!!.setBackgroundResource(R.drawable.textview_border_style_selected)
                    russian_btn!!.setBackgroundResource(R.drawable.textview_border_style)
                } else if (lang === "zh") {
                    chinese_btn!!.setTextColor(resources.getColor(R.color.white))
                    chinese_btn!!.setBackgroundResource(R.drawable.textview_border_style_selected)
                    hindi_btn!!.setBackgroundResource(R.drawable.textview_border_style)
                    russian_btn!!.setBackgroundResource(R.drawable.textview_border_style)
                } else {
                    russian_btn!!.setTextColor(resources.getColor(R.color.white))
                    chinese_btn!!.setBackgroundResource(R.drawable.textview_border_style)
                    hindi_btn!!.setBackgroundResource(R.drawable.textview_border_style)
                    russian_btn!!.setBackgroundResource(R.drawable.textview_border_style_selected)
                }
            }?.addOnFailureListener { exception -> // Recognition failure.
                Log.d(TAG, "Exception : onfailure")
                displayFailure(exception)
            }
        } else {
            showToast("Nothing to translate", context)
        }
    }

    /**
     * Initialize view
     *
     * @param view reference of this fragment
     */
    private fun initView(view: View) {
        rlPrevNext = view.findViewById(R.id.rlPrevNextButton)
        btnBookmarkPage = view.findViewById(R.id.btnBookmark)
        imgPdfView = view.findViewById(R.id.imgPdfView)
        mAudioBtnLl = view.findViewById(R.id.audio_btn_ll)
        mPlay = view.findViewById(R.id.play_audio)
        mStop = view.findViewById(R.id.stop_audio)
        mRepeat = view.findViewById(R.id.repeat_audio)
        btnNext = view.findViewById(R.id.btnNext)
        btnPrev = view.findViewById(R.id.btnPrevious)
        mPlay?.setOnClickListener(this)
        mStop?.setOnClickListener(this)
        mRepeat?.setOnClickListener(this)
        btnPrev?.setOnClickListener(this)
        btnNext?.setOnClickListener(this)
        val animation1 =
            AnimationUtils.loadAnimation(activity, R.anim.zoomin)
        imgPdfView?.startAnimation(animation1)
    }

    /**
     * Throw the exception if occur during TTS
     *
     * @param exception will be throw
     */
    private fun displayFailure(exception: Exception) {
        var error = "Failure. "
        val mlException = exception as MLException
        if (exception is MLException)
        {
            error += "error code: " + mlException.errCode
        }
        showToast(error, context)
    }

    /**
     * Will call this if user want to do translation
     *
     * @param text that we want to translate
     * @param isTranslator required or not
     */
    private fun remoteDisplaySuccess(
        text: String,
        isTranslator: Boolean
    ) {
        if (isTranslator) {
            showTranslatePopUp(text)
        }
    }

    /**
     * Open one dialog that will contain translated text
     *
     * @param text that will be translated
     */
    private fun showTranslatePopUp(text: String) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.translate_dialog_layout)
        hindi_btn = dialog.findViewById(R.id.hindi_lang_btn)
        chinese_btn = dialog.findViewById(R.id.arabic_lang_btn)
        russian_btn = dialog.findViewById(R.id.russian_lang_btn)
        val translated = dialog.findViewById<TextView>(R.id.translated_text_tv)
        translated.text = text
        hindi_btn?.setOnClickListener(
            View.OnClickListener {
                dialog.dismiss()
                remoteTranslator("hi")
            })
        chinese_btn?.setOnClickListener(
            View.OnClickListener {
                dialog.dismiss()
                remoteTranslator("zh")
            })
        russian_btn?.setOnClickListener(
            View.OnClickListener {
                dialog.dismiss()
                remoteTranslator("ru")
            })
        val dialogCloseButton =
            dialog.findViewById<ImageView>(R.id.cancle_btn)
        dialogCloseButton.setOnClickListener { viewDialog: View? -> dialog.dismiss() }
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setApiKey()
        mlConfigs = MLTtsConfig()
        mlKit()
        updateConfig()
        btnBookmarkPage!!.setOnClickListener { bookmarkPdfPage() }
    }

    /**
     * Set the key, required to use ML kit
     */
    private fun setApiKey() {
        val config = AGConnectServicesConfig.fromContext(activity)
        MLApplication.getInstance().apiKey = config.getString(API_KEY)
    }

    /**
     * Get the cloud db zone object.
     */
    fun openCloudDBZone() {
        initializeDb(
            object : DBOpenInterface {
                override fun success(cloudDBZone: CloudDBZone?) {
                    mCloudDBZone = cloudDBZone
                    queryBookmarkList()
                    queryMaxInterestId()
                }

                override fun failure() {}
            })
    }

    /**
     * Query all the bookmarks of a book
     */
    fun queryBookmarkList() {
        if (mCloudDBZone == null) {
            return
        }
        val queryTask =
            mCloudDBZone!!.executeQuery(
                CloudDBZoneQuery.where(Bookmark::class.java)
                    .equalTo("emailid", sp!!.fetchEmailId()),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
        queryTask
            .addOnSuccessListener { snapshot ->
                Log.d(
                    TAG,
                    "Get Bookmark list successfully"
                )
                processQueryResult(snapshot)
            }
            .addOnFailureListener { showToast("on failure", context) }
    }

    /**
     * Return last primary key entered in bookmark table
     */
    protected fun queryMaxInterestId() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val queryTask: Task<CloudDBZoneSnapshot<Bookmark>> =
            CloudDbQueries.instance!!.getMaxBookmarkIdQuery(mCloudDBZone!!)
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Bookmark> ->
                val bookInfoCursor =
                    snapshot.snapshotObjects
                try {
                    while (bookInfoCursor.hasNext()) {
                        val bookmarkId =
                            bookInfoCursor.next()
                        bookmarkMaxId = bookmarkId.bookmarkid!!
                    }
                } catch (e: AGConnectCloudDBException) {
                    Log.w(
                        TAG,
                        "processQueryResult: AGConnectCloudDBException"
                    )
                } finally {
                    snapshot.release()
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(
                    TAG,
                    "addOnFailureListener: queryMaxInterestId"
                )
            }
    }

    /**
     * Get the bookmark details from cloud DB
     *
     * @param snapshot is the bookmark table object for CloudDBZoneSnapshot
     */
    private fun processQueryResult(snapshot: CloudDBZoneSnapshot<Bookmark>?) {
        mCurrentBookDetails = ArrayList()
        Log.d(TAG, "processQueryResult")
        var bookmarkInfoList: ArrayList<Bookmark?> =
            ArrayList()
        bookmarkInfoList = getBookmarks(
            snapshot,
            (bookmarkInfoList as ArrayList<Bookmark>?)!!
        ) as ArrayList<Bookmark?>
        Log.d(
            TAG,
            "processQueryResult success list" + bookmarkInfoList.size.toString()
        )
        for (i in bookmarkInfoList.indices) {
            if (bookmarkInfoList[i]!!.bookId == mCurrentBookId) {
                (mCurrentBookDetails as ArrayList<Int?>).add(bookmarkInfoList[i]!!.pageno)
                Log.d(
                    TAG,
                    "Current book details " + bookmarkInfoList[i]!!.pageno.toString()
                )
            }
        }
        try {
            displayBook()
        } catch (exception: IOException) {
            Log.d(TAG, "IOException ")
        }
    }

    /**
     * Call save or delete the bookmark based on user action
     */
    private fun bookmarkPdfPage() {
        if (btnBookmarkPage?.getBackground()?.constantState
            == resources.getDrawable(R.drawable.nobookmark1).constantState
        ) {
            saveBookmark(currentPage!!.index)
        } else {
            queryDelete()
        }
    }

    /**
     * Save the bookmark into cloud DB
     *
     * @param pageNo which we want to mark as bookmark
     */
    private fun saveBookmark(pageNo: Int) {
        val mBookmark =
            Bookmark()
        mBookmark.emailid = sp!!.fetchEmailId()
        mBookmark.pageno = pageNo
        if (bookmarkMaxId > 0) {
            bookmarkMaxId++
            mBookmark.bookmarkid = bookmarkMaxId
        } else {
            mBookmark.bookmarkid = 1
        }
        mBookmark.bookId = mCurrentBookId
        mBookmark.bookname = mBookName
        if (mCloudDBZone == null) {
            return
        }
        val upsert = mCloudDBZone!!.executeUpsert(mBookmark)
        upsert
            .addOnSuccessListener { snapshot: Int? ->
                Log.d(TAG, "Bookmark added successfully")
                btnBookmarkPage!!.setBackgroundResource(R.drawable.bookmark1)
                mCurrentBookDetails!!.add(currentPage!!.index)
                showToast(
                    "Page " + (currentPage!!.index + 1) + " added as Favourite.",
                    context
                )
            }
            .addOnFailureListener { exception: Exception? ->
                showToast(
                    getString(R.string.bookmark_error),
                    context
                )
            }
    }

    /**
     * Fetch the row that we want to delete from cloud DB
     */
    private fun queryDelete() {
        if (mCloudDBZone == null) {
            return
        }
        val queryTask: Task<CloudDBZoneSnapshot<Bookmark>> =
            CloudDbQueries.instance!!.getDelQuery(
                mCloudDBZone!!,
                sp!!.fetchEmailId(),
                currentPage!!.index,
                mCurrentBookId
            )
        queryTask
            .addOnSuccessListener { snapshot: CloudDBZoneSnapshot<Bookmark>? ->
                Log.d(TAG, "Bookmark deleted successfully")
                var bookmarkDetail: ArrayList<Bookmark> =
                    ArrayList()
                bookmarkDetail = getBookmarks(snapshot, bookmarkDetail) as ArrayList<Bookmark>
                deleteBookmark(bookmarkDetail)
            }
            .addOnFailureListener { e: Exception? ->
                Log.d(
                    TAG,
                    "on failure listener"
                )
            }
    }

    /**
     * Delete the bookmark from cloud DB
     *
     * @param bookmarkDetail is the bookmark detail which we want to delete
     */
    private fun deleteBookmark(bookmarkDetail: List<Bookmark?>) {
        val delete = mCloudDBZone!!.executeDelete(bookmarkDetail)
        delete.addOnSuccessListener { snapshot: Int? ->
            mCurrentBookDetails!!.removeAt(currentPage!!.index)
            btnBookmarkPage!!.setBackgroundResource(R.drawable.nobookmark1)
        }
            .addOnFailureListener { e: Exception? ->
                Log.d(
                    TAG,
                    "Exception failure deleteBookmark"
                )
            }
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
        Log.d(TAG + "filename ", fileName)
        val file =
            File(getSdkDirPath(activity!!), fileName)
        if (file.exists()) {
            Log.d("$TAG filename exist", fileName)
            stopProgressBar()
            downloadedFilePath = file
            showPdfFromFile(file)
        } else {
            Log.d("$TAG filename not exist", fileName)
            downloadPdfFromInternet(mBookUrl)
        }
    }

    /**
     * Download the pdf file from URL
     *
     * @param url of pdf file
     * @throws IOException input output exception
     */
    @Throws(IOException::class)
    private fun downloadPdfFromInternet(url: String?) {
        DownloadTask(activity!!, url!!, object : DownloadPdf {
            override fun onPdfDownloaded(file: File?) {
                stopProgressBar()
                Log.d(TAG, "onPdfDownloaded")
                downloadedFilePath = file
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
                showPage(0)
                readPdf(file!!.path)
            }
        } catch (exception: IOException) {
            Log.d(TAG, "IOException failure show pdf")
        }
    }

    /**
     * Display Pdf page
     *
     * @param index is the page number
     */
    private fun showPage(index: Int) {
        if (pdfRenderer!!.pageCount <= index) {
            return
        }
        // Make sure to close the current page before opening another one.
        if (currentPage != null) {
            currentPage!!.close()
        }
        currentPage = pdfRenderer!!.openPage(index)
        val bitmap = Bitmap.createBitmap(
            currentPage?.width!!, currentPage?.height!!,
            Bitmap.Config.ARGB_8888
        )
        currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        // We are ready to show the Bitmap to user.
        imgPdfView!!.setImageBitmap(bitmap)
        updateUi()
    }

    /**
     * Update the previous and next button according to pdf size
     */
    private fun updateUi() {
        val index = currentPage!!.index
        val pageCount = pdfRenderer!!.pageCount
        btnPrev!!.isEnabled = index != 0
        btnNext!!.isEnabled = index + 1 < pageCount
        if (btnBookmarkPage != null) {
            if (mCurrentBookDetails != null && mCurrentBookDetails!!.contains(currentPage!!.index)) {
                btnBookmarkPage!!.setBackgroundResource(R.drawable.bookmark1)
            } else {
                btnBookmarkPage!!.setBackgroundResource(R.drawable.nobookmark1)
            }
        }
    }

    /**
     * Get the text from PDF file
     *
     * @param path of the pdf file
     * @return text of pdf file
     */
    private fun readPdf(path: String): String {
        var fullpath = path
        if (fullpath.contains(COLON)) {
            fullpath = path.split(COLON).toTypedArray()[1]
        }
        var getText = ""
        try {
            val pdfReader = PdfReader(fullpath)
            getText = getText + (
                    PdfTextExtractor.getTextFromPage(pdfReader, currentPage!!.index + 1)
                        .trim { it <= ' ' }
                            + System.lineSeparator()) // Extracting the content from the different pages
            val arr = getText.split("\\s+".toRegex()).toTypedArray()
            val numberOfWords = 60 // NUMBER OF WORDS THAT YOU NEED
            Log.d("DisplayFragment","arr :"+arr.size);
            var nWords = ""
            if (arr.size <= numberOfWords) {
                mPdfExtractedText = getText
            } else {
                for (i in 0 until numberOfWords) {
                    nWords = nWords + arr[i]
                }
                mPdfExtractedText = nWords
            }
            pdfReader.close()
        } catch (exception: IOException) {
            showToast("IOException", context)
        }
        return getText
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        // Your menu needs to be added here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        return if (itemId == R.id.action_speak) {
            readPdf(downloadedFilePath!!.path)

            Log.d("DisplayBook","text : "+mPdfExtractedText);
            val id = mlTtsEngine!!.speak(mPdfExtractedText, MLTtsEngine.QUEUE_APPEND)
            displayResult("TaskID: $id submit.")
            mAudioBtnLl!!.visibility = View.VISIBLE
            rlPrevNext!!.visibility = View.GONE
            true
        } else if (itemId == R.id.action_translate) {
            mlTtsEngine!!.stop()
            mAudioBtnLl!!.visibility = View.GONE
            rlPrevNext!!.visibility = View.VISIBLE
            remoteTranslator("hi")
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Fetches ML events
     */
    private fun mlKit() {
        mlTtsEngine = MLTtsEngine(mlConfigs)
        mlTtsEngine!!.setTtsCallback(object : MLTtsCallback {
            var str = ""
            override fun onError(taskId: String, err: MLTtsError) {
                str = "TaskID: $taskId, error:$err"
                displayResult(str)
            }

            override fun onWarn(taskId: String, mlTtsWarn: MLTtsWarn) {
                str = "TaskID: $taskId, warn:$mlTtsWarn"
                displayResult(str)
            }

            override fun onRangeStart(
                taskId: String,
                start: Int,
                end: Int
            ) {
                str = "TaskID: $taskId, onRangeStart [$start，$end]"
                displayResult(str)
            }

            override fun onAudioAvailable(
                s: String,
                mlTtsAudioFragment: MLTtsAudioFragment,
                i: Int,
                pair: Pair<Int, Int>,
                bundle: Bundle
            ) {
            }

            override fun onEvent(
                taskId: String,
                eventName: Int,
                bundle: Bundle
            ) {
                str = "TaskID: $taskId, eventName:$eventName"
                if (eventName == MLTtsConstants.EVENT_PLAY_STOP) {
                    str += " " + bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED)
                }
                displayResult(str)
            }
        })
    }

    /**
     * Display result of TTS
     *
     * @param str string message
     */
    private fun displayResult(str: String) {
        val msg = Message()
        val data = Bundle()
        data.putString(HANDLE_KEY, str)
        msg.data = data
        msg.what = HANDLE_CODE
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.search)
        if (item != null) {
            menu.removeItem(R.id.search)
        }
        menu.findItem(R.id.action_translate).isVisible = true
        menu.findItem(R.id.action_speak).isVisible = true
    }

    override fun onClick(view: View) {
        val viewId = view.id
        if (viewId == R.id.repeat_audio) {
            if (mlTtsEngine == null) {
                return
            }
            mlTtsEngine!!.stop()
            mPlay!!.setText(R.string.pause)
            val text = mPdfExtractedText
            val id = mlTtsEngine!!.speak(text, MLTtsEngine.QUEUE_APPEND)
            displayResult("TaskID: $id submit.")
            mAudioBtnLl!!.visibility = View.VISIBLE
            rlPrevNext!!.visibility = View.GONE
        } else if (viewId == R.id.play_audio) {
            isPause = !isPause
            mPlay!!.setText(if (isPause) R.string.resume else R.string.pause)
            if (isPause) {
                mlTtsEngine!!.pause()
            } else {
                mlTtsEngine!!.resume()
            }
        } else if (viewId == R.id.btnPrevious) {
            showPage(currentPage!!.index - 1)
        } else if (viewId == R.id.btnNext) {
            showPage(currentPage!!.index + 1)
        } else {
            mlTtsEngine!!.stop()
            mAudioBtnLl!!.visibility = View.GONE
            rlPrevNext!!.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTtsAudioPlayer()
    }

    /**
     * Stop the TTS engine.
     */
    private fun stopTtsAudioPlayer() {
        mlTtsEngine!!.stop()
        mAudioBtnLl!!.visibility = View.GONE
        rlPrevNext!!.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        stopTtsAudioPlayer()
    }

    override fun onStop() {
        super.onStop()
        stopTtsAudioPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            closeRenderer()
        } catch (e: IOException) {
            Log.e(TAG, "IOException")
        }
    }

    /**
     * Close the render if pdf is not showing
     *
     * @throws IOException input output exception
     */
    @Throws(IOException::class)
    private fun closeRenderer() {
        if (currentPage != null) {
            currentPage!!.close()
        }
        if (pdfRenderer != null) {
            pdfRenderer!!.close()
        }
        if (parcelFileDescriptor != null) {
            parcelFileDescriptor!!.close()
        }
    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
    }

    companion object {
        private val TAG = DisplayBook::class.java.simpleName
        private const val HANDLE_KEY = "text"
        private const val API_KEY = "client/api_key"
        private const val COLON = ":"
        private const val HANDLE_CODE = 1
    }
}