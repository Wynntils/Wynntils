/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;

public class Download extends NetResult {
    private final File localFile;

    // Saved since we might need to get timestamps from the HttpResponse
    private CompletableFuture<HttpResponse<Path>> httpResponse = null;

    public Download(File localFile) {
        super(null); // Only use cached file
        this.localFile = localFile;
    }

    public Download(File localFile, HttpRequest request) {
        super(request);
        this.localFile = localFile;
    }

    public long getResponseTimestamp() {
        if (httpResponse == null) {
            // We have either not yet made the request, or we have read from the cache
            // In either case, the best we can do is to return the current time
            return System.currentTimeMillis();
        }

        try {
            HttpHeaders headers = httpResponse.get().headers();
            OptionalLong a = headers.firstValueAsLong("timestamp");
            if (a.isEmpty()) return System.currentTimeMillis();
            return a.getAsLong();
        } catch (InterruptedException | ExecutionException e) {
            return System.currentTimeMillis();
        }
    }

    protected CompletableFuture<InputStream> getInputStreamFuture() {
        if (request == null) {
            // File is already in downloaded, just read from the cache
            return CompletableFuture.supplyAsync(() -> getFileInputStreamFromCache());
        } else {
            prepareForDownload();
            return getDownloadInputStreamFuture().thenApply(response -> getFileInputStreamFromCache());
        }
    }

    private CompletableFuture<HttpResponse<Path>> getDownloadInputStreamFuture() {
        CompletableFuture<HttpResponse<Path>> future =
                Managers.Net.HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofFile(localFile.toPath()));

        // We must save the response so we can get the timestamp
        this.httpResponse = future;
        return future;
    }

    private InputStream getFileInputStreamFromCache() {
        try {
            return new FileInputStream(localFile);
        } catch (FileNotFoundException e) {
            // This should not happen; we have checked for the file
            WynntilsMod.error("File went missing from cache", e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    private void prepareForDownload() {
        FileUtils.deleteQuietly(localFile);
        try {
            FileUtils.forceMkdirParent(localFile);
        } catch (IOException e) {
            WynntilsMod.error("Failed to create directories needed for " + localFile, e);
        }
    }
}
