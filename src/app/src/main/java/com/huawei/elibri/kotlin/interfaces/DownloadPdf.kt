/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */
package com.huawei.elibri.kotlin.interfaces

import java.io.File

/**
 * An interface for pdf download
 *
 * @author lWX916345
 * @since 01/01/2021
 */
interface DownloadPdf {
    /**
     * Called when pdf file has been downloaded successfully.
     *
     * @param file name
     */
    fun onPdfDownloaded(file: File?)

    /**
     * Called when pdf file has been not been downloaded successfully.
     *
     * @param error message
     */
    fun onError(error: String?)
}