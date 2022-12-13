/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.util.OptionalLong;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;

public class Download extends NetAction {
    private final File localFile;

    public Download(File localFile) {
        super(null);
        this.localFile = localFile;
    }

    public static Download readFromCache(File localFile) {
        // FIXME: implement
        return new Download(localFile);
    }

    public static Download downloadAndStore(File localFile, HttpRequest request) {
        FileUtils.deleteQuietly(localFile);
        // FIXME: implement
        // HttpResponse.BodyHandlers.ofFile(Paths.get("body.txt"))

        // in case of failure:
//        FileUtils.deleteQuietly(cacheFile);

        return new Download(localFile);
    }

    public void onCompletionInputStream(Consumer<InputStream> onCompletion, Consumer<Throwable> onError) {
        // FIXME: implement
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
        } catch (InterruptedException|ExecutionException e) {
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
