/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiResponse extends NetResult {
    // By storing the Future here we assure it will not be GC'ed before we are done with it
    private static final Map<HttpRequest, CompletableFuture<InputStream>> DOWNLOAD_FUTURES = new HashMap<>();

    public ApiResponse(HttpRequest request) {
        super(request);
    }

    protected CompletableFuture<InputStream> getInputStreamFuture() {
        CompletableFuture<InputStream> future = NetManager.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .whenComplete((ignored, exc) -> DOWNLOAD_FUTURES.remove(request))
                .thenApply(HttpResponse::body);

        DOWNLOAD_FUTURES.put(request, future);
        return future;
    }
}
