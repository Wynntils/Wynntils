/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;

public class Download extends NetResult {
    private static final Map<HttpRequest, CompletableFuture<HttpResponse<Path>>> DOWNLOAD_FUTURES = new HashMap<>();

    private final File localFile;

    // Saved since we might need to get timestamps from the HttpResponse
    private CompletableFuture<HttpResponse<Path>> future;

    public Download(File localFile) {
        super(null); // Only use cached file
        this.localFile = localFile;
    }

    public Download(File localFile, HttpRequest request) {
        super(request);
        this.localFile = localFile;
    }

    public long getResponseTimestamp() {
        // FIXME: handle case if we read from cache as fallback!
        try {
            HttpHeaders headers = future.get().headers();
            OptionalLong a = headers.firstValueAsLong("timestamp");
            if (a.isEmpty()) return System.currentTimeMillis();
            return a.getAsLong();
        } catch (InterruptedException | ExecutionException e) {
            WynntilsMod.warn("Cannot retrieve http header timestamp");
            return System.currentTimeMillis();
        }
    }

    protected CompletableFuture<InputStream> getInputStreamFuture() {
        if (request == null) {
            try {
                InputStream inputStream = new FileInputStream(localFile);
                return CompletableFuture.supplyAsync(() -> inputStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return downloadToCacheAsync().thenApply(response -> {
                try {
                    return new FileInputStream(response.body().toFile());
                } catch (FileNotFoundException e) {
                    // FIXME
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private CompletableFuture<HttpResponse<Path>> downloadToCacheAsync() {
        FileUtils.deleteQuietly(localFile);
        localFile.getParentFile().mkdirs();
        future = NetManager.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofFile(localFile.toPath()))
                .whenComplete((ignored, exc) -> {
                    DOWNLOAD_FUTURES.remove(request);
                });
        // in case of failure:
        //        FileUtils.deleteQuietly(cacheFile);
        DOWNLOAD_FUTURES.put(request, future);
        return future;
    }
}
