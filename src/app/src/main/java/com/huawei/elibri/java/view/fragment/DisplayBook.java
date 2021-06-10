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

package com.huawei.elibri.java.view.fragment;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.interfaces.DownloadPdf;
import com.huawei.elibri.java.service.model.Bookmark;
import com.huawei.elibri.java.service.repository.CloudDbQueries;
import com.huawei.elibri.java.utility.DownloadTask;
import com.huawei.elibri.java.utility.Util;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory;
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting;
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator;
import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Open the book for reading with TTs functionality
 *
 * @author lWX916345
 * @since 25-10-2020
 */
public class DisplayBook extends BookBaseFragment
        implements View.OnClickListener {
    private static final String TAG = DisplayBook.class.getSimpleName();
    private static final String HANDLE_KEY = "text";
    private static final String API_KEY = "client/api_key";
    private static final String COLON = ":";
    private static final int HANDLE_CODE = 1;
    private int bookmarkMaxId;
    private Button btnBookmarkPage;
    private Button btnNext;
    private Button btnPrev;
    private ImageView imgPdfView;
    private File downloadedFilePath;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private String mBookUrl;
    private String mBookName;
    private boolean isPause;
    private float speedVal = 1.0f;
    private float volumeVal = 1.0f;
    private MLTtsEngine mlTtsEngine;
    private MLTtsConfig mlConfigs;
    private String defaultLanguageCode = "en-US";
    private String defaultSpeakerCode = "en-US-st-1";
    private Button mPlay;
    private Button mStop;
    private Button mRepeat;
    private MLRemoteTranslator translator;
    private CloudDBZone mCloudDBZone;
    private SavedPreference sp;
    private int mCurrentBookId;
    private List<Integer> mCurrentBookDetails;
    private LinearLayout mAudioBtnLl;
    private RelativeLayout rlPrevNext;
    private String mPdfExtractedText = "";
    private TextView russian_btn;
    private TextView hindi_btn;
    private TextView chinese_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_book, container, false);
        initView(view);
        sp = SavedPreference.getInstance(getContext());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mCurrentBookId = bundle.getInt("BOOK_ID", 101);
            mBookUrl = bundle.getString("BOOK_URL", "");
            mBookName = bundle.getString("BOOK_NAME", "");
        }
        openCloudDBZone();
        btnBookmarkPage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookmarkPdfPage();
                    }
                });
        return view;
    }

    /**
     * To set up TTS configuration
     */
    private void updateConfig() {
        MLTtsConfig mlTtsConfig =
                new MLTtsConfig()
                        .setVolume(volumeVal)
                        .setSpeed(speedVal)
                        .setLanguage(defaultLanguageCode)
                        .setPerson(defaultSpeakerCode);
        mlTtsEngine.updateConfig(mlTtsConfig);
    }

    /**
     * Translate the content using ML kit
     *
     * @param lang in which we want to translate the content
     */
    private void remoteTranslator(String lang) {
        Util.setProgressBar(getContext());
        // Create an analyzer. You can customize the analyzer by creating MLRemoteTranslateSetting
        MLRemoteTranslateSetting setting = new MLRemoteTranslateSetting.Factory().setTargetLangCode(lang).create();
        this.translator = MLTranslatorFactory.getInstance().getRemoteTranslator(setting);
        String sourceText = readPdf(downloadedFilePath.getPath());
        if (sourceText != null) {
            Task<String> task = this.translator.asyncTranslate(sourceText);
            task.addOnSuccessListener(
                    new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String text) {
                            Util.stopProgressBar();
                            DisplayBook.this.remoteDisplaySuccess(text, true);

                            if (lang == "hi") {
                                hindi_btn.setTextColor(getResources().getColor(R.color.white));
                                chinese_btn.setBackgroundResource(R.drawable.textview_border_style);
                                hindi_btn.setBackgroundResource(R.drawable.textview_border_style_selected);
                                russian_btn.setBackgroundResource(R.drawable.textview_border_style);
                            } else if (lang == "zh") {
                                chinese_btn.setTextColor(getResources().getColor(R.color.white));
                                chinese_btn.setBackgroundResource(R.drawable.textview_border_style_selected);
                                hindi_btn.setBackgroundResource(R.drawable.textview_border_style);
                                russian_btn.setBackgroundResource(R.drawable.textview_border_style);

                            } else {
                                russian_btn.setTextColor(getResources().getColor(R.color.white));
                                chinese_btn.setBackgroundResource(R.drawable.textview_border_style);
                                hindi_btn.setBackgroundResource(R.drawable.textview_border_style);
                                russian_btn.setBackgroundResource(R.drawable.textview_border_style_selected);
                            }
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(Exception exception) {
                                    // Recognition failure.
                                    Log.d(TAG, "Exception : onfailure");
                                    DisplayBook.this.displayFailure(exception);
                                }
                            });
        } else {
            showToast("Nothing to translate", getContext());
        }
    }

    /**
     * Initialize view
     *
     * @param view reference of this fragment
     */
    private void initView(View view) {
        rlPrevNext = view.findViewById(R.id.rlPrevNextButton);
        btnBookmarkPage = view.findViewById(R.id.btnBookmark);
        imgPdfView = view.findViewById(R.id.imgPdfView);
        mAudioBtnLl = view.findViewById(R.id.audio_btn_ll);
        mPlay = view.findViewById(R.id.play_audio);
        mStop = view.findViewById(R.id.stop_audio);
        mRepeat = view.findViewById(R.id.repeat_audio);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrev = view.findViewById(R.id.btnPrevious);
        mPlay.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mRepeat.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        Animation animation1 = AnimationUtils.loadAnimation(getActivity(), R.anim.zoomin);
        imgPdfView.startAnimation(animation1);
    }

    /**
     * Throw the exception if occur during TTS
     *
     * @param exception will be throw
     */
    private void displayFailure(Exception exception) {
        String error = "Failure. ";
        MLException mlException = (MLException) exception;
        if (exception instanceof MLException) {
            error += "error code: " + mlException.getErrCode();
        }
        showToast(error, getContext());
    }

    /**
     * Will call this if user want to do translation
     *
     * @param text that we want to translate
     * @param isTranslator required or not
     */

    private void remoteDisplaySuccess(String text, boolean isTranslator) {
        if (isTranslator) {
            showTranslatePopUp(text);
        }
    }

    /**
     * Open one dialog that will contain translated text
     *
     * @param text that will be translated
     */
    private void showTranslatePopUp(String text) {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.translate_dialog_layout);
        hindi_btn = dialog.findViewById(R.id.hindi_lang_btn);
        chinese_btn = dialog.findViewById(R.id.arabic_lang_btn);
        russian_btn = dialog.findViewById(R.id.russian_lang_btn);
        TextView translated = dialog.findViewById(R.id.translated_text_tv);
        translated.setText(text);
        hindi_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        remoteTranslator("hi");
                    }
                });

        chinese_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        remoteTranslator("zh");
                    }
                });

        russian_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        remoteTranslator("ru");
                    }
                });
        ImageView dialogCloseButton = dialog.findViewById(R.id.cancle_btn);
        dialogCloseButton.setOnClickListener(viewDialog -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setApiKey();
        mlConfigs = new MLTtsConfig();
        mlKit();
        updateConfig();
        btnBookmarkPage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookmarkPdfPage();
                    }
                });
    }

    /**
     * Set the key, required to use ML kit
     */
    private void setApiKey() {
        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(getActivity());
        MLApplication.getInstance().setApiKey(config.getString(API_KEY));
    }

    /**
     * Get the cloud db zone object.
     */
    public void openCloudDBZone() {
        initializeDb(
                new DBOpenInterface() {
                    @Override
                    public void success(CloudDBZone cloudDBZone) {
                        mCloudDBZone = cloudDBZone;
                        queryBookmarkList();
                        queryMaxInterestId();
                    }

                    @Override
                    public void failure() {
                    }
                });
    }

    /**
     * Query all the bookmarks of a book
     */
    public void queryBookmarkList() {
        if (mCloudDBZone == null) {
            return;
        }
        Task<CloudDBZoneSnapshot<Bookmark>> queryTask =
                mCloudDBZone.executeQuery(
                        CloudDBZoneQuery.where(Bookmark.class).equalTo("emailid", sp.fetchEmailId()),
                        CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<CloudDBZoneSnapshot<Bookmark>>() {
                            @Override
                            public void onSuccess(CloudDBZoneSnapshot<Bookmark> snapshot) {
                                Log.d(TAG, "Get Bookmark list successfully");
                                processQueryResult(snapshot);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception exp) {
                                showToast("on failure", getContext());
                            }
                        });
    }

    /**
     * Return last primary key entered in bookmark table
     */
    protected void queryMaxInterestId() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<Bookmark>> queryTask =
                CloudDbQueries.getInstance().getMaxBookmarkIdQuery(mCloudDBZone);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            CloudDBZoneObjectList<Bookmark> bookInfoCursor = snapshot.getSnapshotObjects();
                            try {
                                while (bookInfoCursor.hasNext()) {
                                    Bookmark bookmarkId = bookInfoCursor.next();
                                    bookmarkMaxId = bookmarkId.getBookmarkid();
                                }
                            } catch (AGConnectCloudDBException e) {
                                Log.w(TAG, "processQueryResult: AGConnectCloudDBException");
                            } finally {
                                snapshot.release();
                            }
                        })
                .addOnFailureListener(e ->  Log.w(TAG, "addOnFailureListener: queryMaxInterestId"));
    }

    /**
     * Get the bookmark details from cloud DB
     *
     * @param snapshot is the bookmark table object for CloudDBZoneSnapshot
     */
    private void processQueryResult(CloudDBZoneSnapshot<Bookmark> snapshot) {
        mCurrentBookDetails = new ArrayList<>();
        Log.d(TAG, "processQueryResult");
        List<Bookmark> bookmarkInfoList = new ArrayList<>();
        bookmarkInfoList = getBookmarks(snapshot, bookmarkInfoList);
        Log.d(TAG, "processQueryResult success list" +  String.valueOf(bookmarkInfoList.size()));
        for (int i = 0; i < bookmarkInfoList.size(); i++) {
            if (bookmarkInfoList.get(i).getBookId().equals(mCurrentBookId)) {
                mCurrentBookDetails.add(bookmarkInfoList.get(i).getPageno());
                Log.d(TAG, "Current book details " + bookmarkInfoList.get(i).getPageno().toString());
            }
        }
        try {
            displayBook();
        } catch (IOException exception) {
            Log.d(TAG, "IOException ");
        }
    }

    /**
     * Call save or delete the bookmark based on user action
     */
    private void bookmarkPdfPage() {
        if (btnBookmarkPage
                .getBackground()
                .getConstantState()
                .equals(getResources().getDrawable(R.drawable.nobookmark1).getConstantState())) {
            saveBookmark(currentPage.getIndex());
        } else {
            queryDelete();
        }
    }

    /**
     * Save the bookmark into cloud DB
     *
     * @param pageNo which we want to mark as bookmark
     */
    private void saveBookmark(int pageNo) {
        Bookmark mBookmark = new Bookmark();
        mBookmark.setEmailid(sp.fetchEmailId());
        mBookmark.setPageno(pageNo);
        if (bookmarkMaxId > 0) {
            bookmarkMaxId++;
            mBookmark.setBookmarkid(bookmarkMaxId);
        } else {
            mBookmark.setBookmarkid(1);
        }
        mBookmark.setBookId(mCurrentBookId);
        mBookmark.setBookname(mBookName);
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> upsert = mCloudDBZone.executeUpsert(mBookmark);
        upsert
                .addOnSuccessListener(
                        snapshot -> {
                            Log.d(TAG, "Bookmark added successfully");
                            btnBookmarkPage.setBackgroundResource(R.drawable.bookmark1);
                            mCurrentBookDetails.add(currentPage.getIndex());
                            showToast("Page " + (currentPage.getIndex() + 1) + " added as Favourite.", getContext());
                        })
                .addOnFailureListener(
                        exception -> {
                            showToast(getString(R.string.bookmark_error), getContext());
                        });
    }

    /**
     * Fetch the row that we want to delete from cloud DB
     */
    private void queryDelete() {
        if (mCloudDBZone == null) {
            return;
        }
        Task<CloudDBZoneSnapshot<Bookmark>> queryTask =
                CloudDbQueries.getInstance().getDelQuery(mCloudDBZone, sp.fetchEmailId(),
                currentPage.getIndex(), mCurrentBookId);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            Log.d(TAG, "Bookmark deleted successfully");
                            List<Bookmark> bookmarkDetail = new ArrayList<Bookmark>();
                            bookmarkDetail = getBookmarks(snapshot, bookmarkDetail);
                            deleteBookmark(bookmarkDetail);
                        })
                .addOnFailureListener(e -> Log.d(TAG, "on failure listener" ));
    }

    /**
     * Delete the bookmark from cloud DB
     *
     * @param bookmarkDetail is the bookmark detail which we want to delete
     */
    private void deleteBookmark(List<Bookmark> bookmarkDetail) {
        Task<Integer> delete = mCloudDBZone.executeDelete(bookmarkDetail);
        delete.addOnSuccessListener(
                snapshot -> {
                    mCurrentBookDetails.remove(currentPage.getIndex());
                    btnBookmarkPage.setBackgroundResource(R.drawable.nobookmark1);
                })
                .addOnFailureListener(
                        e -> Log.d(TAG, "Exception failure deleteBookmark"));
    }

    /**
     * Open the pdf file either from internal storage or download it from internet
     *
     * @throws IOException while getting the file path
     */
    private void displayBook() throws IOException {
        Util.setProgressBar(getContext());
        String fileName = mBookUrl.substring(mBookUrl.lastIndexOf('/') + 1, mBookUrl.length());
        Log.d(TAG + "filename ", fileName);
        File file = new File(Util.getSdkDirPath(getActivity()), fileName);
        if (file.exists()) {
            Log.d(TAG + " filename exist", fileName);
            Util.stopProgressBar();
            downloadedFilePath = file;
            showPdfFromFile(file);
        } else {
            Log.d(TAG + " filename not exist", fileName);
            downloadPdfFromInternet(mBookUrl);
        }
    }

    /**
     * Download the pdf file from URL
     *
     * @param url of pdf file
     * @throws IOException input output exception
     */
    private void downloadPdfFromInternet(String url) throws IOException {
        new DownloadTask(getActivity(), url, new DownloadPdf() {
            @Override
            public void onPdfDownloaded(File file) {
                Util.stopProgressBar();
                Log.d(TAG, "onPdfDownloaded");
                downloadedFilePath = file;
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
                showPage(0);
                readPdf(file.getPath());
            }
        } catch (IOException exception) {
            Log.d(TAG, "IOException failure show pdf");
        }
    }

    /**
     * Display Pdf page
     *
     * @param index is the page number
     */
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (currentPage != null) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        imgPdfView.setImageBitmap(bitmap);
        updateUi();
    }

    /**
     * Update the previous and next button according to pdf size
     */
    private void updateUi() {
        int index = currentPage.getIndex();
        int pageCount = pdfRenderer.getPageCount();
        btnPrev.setEnabled(index != 0);
        btnNext.setEnabled(index + 1 < pageCount);

        if (btnBookmarkPage != null) {
            if (mCurrentBookDetails != null && mCurrentBookDetails.contains(currentPage.getIndex())) {
                btnBookmarkPage.setBackgroundResource(R.drawable.bookmark1);
            } else {
                btnBookmarkPage.setBackgroundResource(R.drawable.nobookmark1);
            }
        }
    }

    /**
     * Get the text from PDF file
     *
     * @param path of the pdf file
     * @return text of pdf file
     */
    private String readPdf(String path) {
        String fullpath = path;
        if (fullpath.contains(COLON)) {
            fullpath = path.split(COLON)[1];
        }
        String getText = "";
        try {
            PdfReader pdfReader = new PdfReader(fullpath);
            getText =
                    getText.concat(
                            PdfTextExtractor.getTextFromPage(pdfReader, currentPage.getIndex() + 1).trim()
                                    + System.lineSeparator()); // Extracting the content from the different pages
            Log.d("DisplayBook", "getText  : " + getText);
            String[] arr = getText.split("\\s+");
            int numberOfWords = 60; // NUMBER OF WORDS THAT YOU NEED
            String nWords = "";
            if (arr.length <= numberOfWords) {
                mPdfExtractedText = getText;
            } else {
                for (int i = 0; i < numberOfWords; i++) {
                    nWords = nWords.concat(arr[i]);
                }

                mPdfExtractedText = nWords;
            }
            pdfReader.close();
        } catch (IOException exception) {
            showToast("IOException", getContext());
        }
        return getText;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Your menu needs to be added here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_speak) {
            readPdf(downloadedFilePath.getPath());
            Log.d("DisplayBook", "text receive : " + mPdfExtractedText);
            String id = mlTtsEngine.speak(mPdfExtractedText, MLTtsEngine.QUEUE_APPEND);
            displayResult("TaskID: " + id + " submit.");
            mAudioBtnLl.setVisibility(View.VISIBLE);
            rlPrevNext.setVisibility(View.GONE);
            return true;
        } else if (itemId == R.id.action_translate) {
            mlTtsEngine.stop();
            mAudioBtnLl.setVisibility(View.GONE);
            rlPrevNext.setVisibility(View.VISIBLE);
            this.remoteTranslator("hi");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Fetches ML events
     */
    private void mlKit() {
        mlTtsEngine = new MLTtsEngine(mlConfigs);
        mlTtsEngine.setTtsCallback(new MLTtsCallback() {
            String str = "";

            @Override
            public void onError(String taskId, MLTtsError err) {
                str = "TaskID: " + taskId + ", error:" + err;
                displayResult(str);
            }

            @Override
            public void onWarn(String taskId, MLTtsWarn mlTtsWarn) {
                str = "TaskID: " + taskId + ", warn:" + mlTtsWarn;
                displayResult(str);
            }

            @Override
            public void onRangeStart(String taskId, int start, int end) {
                str = "TaskID: " + taskId + ", onRangeStart [" + start + "，" + end + "]";
                displayResult(str);
            }

            @Override
            public void onAudioAvailable(
                    String s,
                    MLTtsAudioFragment mlTtsAudioFragment,
                    int i,
                    Pair<Integer, Integer> pair,
                    Bundle bundle) {
            }

            @Override
            public void onEvent(String taskId, int eventName, Bundle bundle) {
                str = "TaskID: " + taskId + ", eventName:" + eventName;
                if (eventName == MLTtsConstants.EVENT_PLAY_STOP) {
                    str += " " + bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED);
                }
                displayResult(str);
            }
        });
    }

    /**
     * Display result of TTS
     *
     * @param str string message
     */
    private void displayResult(String str) {
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString(HANDLE_KEY, str);
        msg.setData(data);
        msg.what = HANDLE_CODE;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.search);
        if (item != null) {
            menu.removeItem(R.id.search);
        }
        menu.findItem(R.id.action_translate).setVisible(true);
        menu.findItem(R.id.action_speak).setVisible(true);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.repeat_audio) {
            if (mlTtsEngine == null) {
                return;
            }
            mlTtsEngine.stop();
            mPlay.setText(R.string.pause);
            String text = mPdfExtractedText;
            String id = mlTtsEngine.speak(text, MLTtsEngine.QUEUE_APPEND);
            displayResult("TaskID: " + id + " submit.");
            mAudioBtnLl.setVisibility(View.VISIBLE);
            rlPrevNext.setVisibility(View.GONE);
        } else if (viewId == R.id.play_audio) {
            isPause = !isPause;
            mPlay.setText(isPause ? R.string.resume : R.string.pause);
            if (isPause) {
                mlTtsEngine.pause();
            } else {
                mlTtsEngine.resume();
            }
        } else if (viewId == R.id.btnPrevious) {
            showPage(currentPage.getIndex() - 1);
        } else if (viewId == R.id.btnNext) {
            showPage(currentPage.getIndex() + 1);
        } else {
            mlTtsEngine.stop();
            mAudioBtnLl.setVisibility(View.GONE);
            rlPrevNext.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTtsAudioPlayer();
    }

    /**
     * Stop the TTS engine.
     */
    private void stopTtsAudioPlayer() {
        mlTtsEngine.stop();
        mAudioBtnLl.setVisibility(View.GONE);
        rlPrevNext.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTtsAudioPlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTtsAudioPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            closeRenderer();
        } catch (IOException e) {
           Log.e(TAG,"IOException");
        }
    }

    /**
     * Close the render if pdf is not showing
     *
     * @throws IOException input output exception
     */
    private void closeRenderer() throws IOException {
        if (currentPage != null) {
            currentPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        if (parcelFileDescriptor != null) {
            parcelFileDescriptor.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
    }
}