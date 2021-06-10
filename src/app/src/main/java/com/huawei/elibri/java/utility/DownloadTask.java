/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.utility;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.huawei.elibri.java.interfaces.DownloadPdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Used to download the pdf file from internet
 *
 * @since: 26-11-2020
 */
public class DownloadTask {
    private static final String TAG = "Download Task";
    private Context context;
    private DownloadPdf callback;
    private String downloadUrl = "";
    private String downloadFileName = "";

    /**
     * Constructor of the class
     *
     * @param context of the class
     * @param downloadUrl is the url of pdf file
     * @param interfaceDownload to get the downloaded pdf result
     */
    public DownloadTask(Context context, String downloadUrl, DownloadPdf interfaceDownload) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        callback =  interfaceDownload;
        downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length());
        new DownloadingTask().execute();
    }

    /**
     * Async task to download the pdf file
     */
    private class DownloadingTask extends AsyncTask<Void, Void, Void> {
        private File outputFile = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    File pdfFile = new File(Util.getSdkDirPath(context), downloadFileName);  // -> filename = maven.pdf
                    callback.onPdfDownloaded(pdfFile);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Error in getting the file");
                        }
                    }, Constants.DELAY_MILLIS);
                    Log.e(TAG, "Download Failed");
                }
            } catch (IOException e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    }
                }, Constants.DELAY_MILLIS);
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                }
                outputFile = new File(Util.getSdkDirPath(context), downloadFileName);
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();
            } catch (IOException exception) {
                outputFile = null;
                Log.e(TAG, "Download Error Exception " + exception.getLocalizedMessage());
            }
            return null;
        }
    }
}
