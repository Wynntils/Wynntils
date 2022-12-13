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
    private static final Map<HttpRequest, CompletableFuture<HttpResponse<InputStream>>> DOWNLOAD_FUTURES =
            new HashMap<>();

    public ApiResponse(HttpRequest request) {
        super(request);
    }

    protected CompletableFuture<InputStream> getInputStreamFuture() {
        CompletableFuture<HttpResponse<InputStream>> future = NetManager.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .whenComplete((ignored, exc) -> DOWNLOAD_FUTURES.remove(request));
        DOWNLOAD_FUTURES.put(request, future);
        return future.thenApply(HttpResponse::body);
    }
}
