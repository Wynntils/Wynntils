/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.request;

import com.wynntils.core.Reference;
import com.wynntils.core.webapi.LoadingPhase;
import com.wynntils.utils.objects.ThrowingBiPredicate;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Specifies how a request is to be carried out by {@link RequestHandler}, as well as handling for
 * the request. Built through {@link RequestBuilder}
 */
public class Request {
    final String url;
    final String id;
    final int parallelGroup;
    final ThrowingBiPredicate<URLConnection, byte[], IOException> handler;
    final boolean useCacheAsBackup;
    private final RequestErrorHandler onError;
    private final Map<String, String> headers;
    private final int timeout;

    final File cacheFile;
    final Predicate<byte[]> cacheValidator;

    LoadingPhase currentlyHandling = LoadingPhase.UNLOADED;

    Request(
            String url,
            String id,
            int parallelGroup,
            ThrowingBiPredicate<URLConnection, byte[], IOException> handler,
            boolean useCacheAsBackup,
            RequestErrorHandler onError,
            Map<String, String> headers,
            File cacheFile,
            Predicate<byte[]> cacheValidator,
            int timeout) {
        this.url = url;
        this.id = id;
        this.parallelGroup = parallelGroup;
        this.handler = handler;
        this.useCacheAsBackup = useCacheAsBackup;
        this.onError = onError;
        this.headers = headers;
        this.cacheFile = cacheFile;
        this.cacheValidator = cacheValidator;
        this.timeout = timeout;
    }

    public void onError() {
        onError.invoke();
    }

    public HttpURLConnection establishConnection() throws IOException {
        HttpURLConnection st = (HttpURLConnection) new URL(url).openConnection();
        st.setRequestProperty(
                "User-Agent",
                "WynntilsClient v" + Reference.VERSION + "/B" + Reference.BUILD_NUMBER);
        if (!headers.isEmpty()) headers.forEach(st::addRequestProperty);

        st.setConnectTimeout(timeout);
        st.setReadTimeout(timeout);
        return st;
    }

    @FunctionalInterface
    public interface RequestErrorHandler {
        void invoke();
    }
}
