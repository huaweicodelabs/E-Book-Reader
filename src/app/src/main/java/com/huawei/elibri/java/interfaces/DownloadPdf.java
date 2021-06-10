/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.interfaces;

import java.io.File;

/**
 * An interface for pdf download
 *
 * @author lWX916345
 * @since 01/01/2021
 */
public interface DownloadPdf {
    /**
     * Called when pdf file has been downloaded successfully.
     *
     * @param file name
     */
    void onPdfDownloaded(File file);

    /**
     * Called when pdf file has been not been downloaded successfully.
     *
     * @param error message
     */
    void onError(String error);
}
