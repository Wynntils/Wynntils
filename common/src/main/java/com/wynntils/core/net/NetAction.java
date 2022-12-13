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

public class NetAction {
    static Map<HttpRequest, CompletableFuture<HttpResponse<InputStream>>> currentRequests = new HashMap<>();
    static Map<HttpRequest, CompletableFuture<Void>> currentNewRequests = new HashMap<>();

    HttpRequest request;
    CompletableFuture<HttpResponse<InputStream>> future;
    private CompletableFuture<Void> newFuture;

    public NetAction(HttpRequest request) {
        this.request = request;
    }

    protected CompletableFuture<HttpResponse<InputStream>> getHttpResponseAsync() {
        future = NetManager11.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .whenComplete((ignored, exc) -> {
                    currentRequests.remove(request);
                });
        currentRequests.put(request, future);
        return future;
    }

    protected CompletableFuture<InputStream> getInputStreamAsync() {
        return getHttpResponseAsync().thenApply(HttpResponse::body);
    }

    protected void storeNewFuture(CompletableFuture<Void> newFuture) {
        CompletableFuture<Void> newFuture2 = newFuture.whenComplete((ignored, exc) -> {
            currentRequests.remove(request); });
        currentNewRequests.put(request, newFuture2);
        this.newFuture = newFuture2;
    }

}
