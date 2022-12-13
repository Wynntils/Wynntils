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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;

public class Download extends NetAction {
    private final File localFile;
    private CompletableFuture<HttpResponse<Path>> future_dl;
    private Map<HttpRequest, CompletableFuture<HttpResponse<Path>>> currentRequests_dl =
            new HashMap<HttpRequest, CompletableFuture<HttpResponse<Path>>>();

    public Download(File localFile) {
        super(null);
        this.localFile = localFile;
    }

    public Download(File localFile, HttpRequest request) {
        super(request);
        this.localFile = localFile;
    }

    protected CompletableFuture<HttpResponse<Path>> cacheHttpResponseAsync() {
        FileUtils.deleteQuietly(localFile);
        future_dl = NetManager.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofFile(localFile.toPath()))
                .whenComplete((ignored, exc) -> {
                    currentRequests_dl.remove(request);
                });
        // in case of failure:
        //        FileUtils.deleteQuietly(cacheFile);
        currentRequests_dl.put(request, future_dl);
        return future_dl;
    }

    protected CompletableFuture<InputStream> cacheInputStreamAsync() {
        if (request == null) {
            try {
                InputStream inputStream = new FileInputStream(localFile);
                return CompletableFuture.supplyAsync(() -> inputStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return cacheHttpResponseAsync().thenApply(response -> {
                try {
                    return new FileInputStream(response.body().toFile());
                } catch (FileNotFoundException e) {
                    // FIXME
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static Download readFromCache(File localFile) {
        return new Download(localFile);
    }

    public static Download downloadAndStore(File localFile, HttpRequest request) {
        return new Download(localFile, request);
    }

    public void onCompletionInputStream(Consumer<InputStream> onCompletion, Consumer<Throwable> onError) {
        CompletableFuture newFuture;
        newFuture = cacheInputStreamAsync()
                .thenAccept((is) -> onCompletion.accept(is))
                .exceptionally(e -> {
                    // FIXME: fix error handling correctly!
                    onError.accept(e);
                    return null;
                });
        storeNewFuture(newFuture);
    }

    public void onCompletionInputStream(Consumer<InputStream> onCompletion) {
        onCompletionInputStream(onCompletion, onError -> {
            WynntilsMod.warn("Error while reading resource");
        });
    }

    public void onCompletion(Consumer<Reader> onCompletion) {
        onCompletionInputStream(is -> onCompletion.accept(new InputStreamReader(is)));
    }

    public long getTimestamp() {
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

    public boolean waitForCompletion(int timeOutMs) {
        // FIXME: handle case where we read from cache
        try {
            future.get(timeOutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // if timeout is reached, return false
            return false;
        }
        return true;
    }
}
