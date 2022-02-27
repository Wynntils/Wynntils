/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */

package com.wynntils.core.webapi.request;

import com.wynntils.utils.objects.ThrowingBiPredicate;
import com.wynntils.utils.objects.ThrowingConsumer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Map;
import java.util.function.Predicate;

public class PostRequest extends Request {

    private ThrowingConsumer<HttpURLConnection, IOException> writer;

    PostRequest(String url, String id, int parallelGroup, ThrowingBiPredicate<URLConnection, byte[], IOException> handler, boolean useCacheAsBackup, RequestErrorHandler onError, Map<String, String> headers, File cacheFile, Predicate<byte[]> cacheValidator, int timeout, ThrowingConsumer<HttpURLConnection, IOException> writer) {
        super(url, id, parallelGroup, handler, useCacheAsBackup, onError, headers, cacheFile, cacheValidator, timeout);
        this.writer = writer;
    }

    @Override
    public HttpURLConnection establishConnection() throws IOException {
        HttpURLConnection st = super.establishConnection();
        st.setDoOutput(true);
        st.setRequestMethod("POST");
        writer.accept(st);
        return st;
    }
}
