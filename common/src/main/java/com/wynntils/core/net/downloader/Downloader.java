/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.downloader;

import java.io.File;
import java.net.URI;

public class Downloader {
    public static DownloadableResource download(URI uri, File localFile, String id) {
        downloadToLocal(uri, localFile);
        return new DownloadableResource(localFile);
    }

    public static DownloadableResource download(String uri, File localFile, String id) {
        return download(URI.create(uri), localFile, id);
    }

    public static DownloadableResource downloadMd5(URI uri, File localFile, String expectedHash, String id) {
        if (!checkLocalHash(localFile, expectedHash)) {
            downloadToLocal(uri, localFile);
        }
        return new DownloadableResource(localFile);
    }

    public static DownloadableResource downloadMd5(String uri, File localFile, String expectedHash, String id) {
        return downloadMd5(URI.create(uri), localFile, expectedHash, id);
    }

    private static boolean checkLocalHash(File localFile, String expectedHash) {
        // FIXME: implement
        return false;
    }

    private static void downloadToLocal(URI uri, File localFile) {
        // FIXME: implement
    }
}
