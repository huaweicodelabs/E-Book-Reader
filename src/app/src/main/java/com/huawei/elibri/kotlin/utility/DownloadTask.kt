/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.utility

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import com.huawei.elibri.kotlin.interfaces.DownloadPdf
import com.huawei.elibri.kotlin.utility.Util.getSdkDirPath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Used to download the pdf file from internet
 *
 * @since: 26-11-2020
 */
@Suppress("DEPRECATION")
class DownloadTask(
    private val context: Context,
    downloadUrl: String,
    interfaceDownload: DownloadPdf
) {
    private val callback: DownloadPdf
    private var downloadUrl = ""
    private var downloadFileName = ""

    /**
     * Async task to download the pdf file
     */
    private inner class DownloadingTask :
        AsyncTask<Void?, Void?, Void?>() {
        private var outputFile: File? = null
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: Void?) {
            try {
                if (outputFile != null) {
                    val pdfFile = File(
                        getSdkDirPath(context),
                        downloadFileName
                    ) // -> filename = maven.pdf
                    callback.onPdfDownloaded(pdfFile)
                } else {
                    Handler().postDelayed(
                        { callback.onError("Error in getting the file") },
                        Constants.DELAY_MILLIS.toLong()
                    )
                    Log.e(
                        TAG,
                        "Download Failed"
                    )
                }
            } catch (e: IOException) {
                Handler().postDelayed({ }, Constants.DELAY_MILLIS.toLong())
                Log.e(
                    TAG,
                    "Download Failed with Exception - " + e.localizedMessage
                )
            }
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                val url = URL(downloadUrl)
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(
                        TAG,
                        "Server returned HTTP " + connection.responseCode
                                + " " + connection.responseMessage
                    )
                }
                outputFile = File(
                    getSdkDirPath(context),
                    downloadFileName
                )
                if (!outputFile!!.exists()) {
                    outputFile!!.createNewFile()
                    Log.e(
                        TAG,
                        "File Created"
                    )
                }
                val fos = FileOutputStream(outputFile)
                val `is` = connection.inputStream
                val buffer = ByteArray(1024)
                var len1 = 0
                while (`is`.read(buffer).also { len1 = it } != -1) {
                    fos.write(buffer, 0, len1)
                }
                fos.close()
                `is`.close()
            } catch (exception: IOException) {
                outputFile = null
                Log.e(
                    TAG,
                    "Download Error Exception " + exception.localizedMessage
                )
            }
            return null
        }
    }

    companion object {
        private const val TAG = "Download Task"
    }

    /**
     * Constructor of the class
     *
     * @param context of the class
     * @param downloadUrl is the url of pdf file
     * @param interfaceDownload to get the downloaded pdf result
     */
    init {
        this.downloadUrl = downloadUrl
        callback = interfaceDownload
        downloadFileName =
            downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length)
        DownloadingTask().execute()
    }
}