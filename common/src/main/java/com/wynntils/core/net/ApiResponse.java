/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ApiResponse {
    static Map<HttpRequest, CompletableFuture<HttpResponse<InputStream>>> downloadFutures = new HashMap<>();
    static Map<HttpRequest, CompletableFuture<Void>> processFutures = new HashMap<>();

    HttpRequest request;

    public ApiResponse(HttpRequest request) {
        this.request = request;
    }

    protected CompletableFuture<HttpResponse<InputStream>> getHttpResponseAsync() {
        CompletableFuture<HttpResponse<InputStream>> future = NetManager.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .whenComplete((ignored, exc) -> {
                    downloadFutures.remove(request);
                });
        downloadFutures.put(request, future);
        return future;
    }

    protected CompletableFuture<InputStream> getInputStreamAsync() {
        return getHttpResponseAsync().thenApply(HttpResponse::body);
    }

    public void handleJsonObject(Consumer<JsonObject> handler, Consumer<Throwable> errorHandler) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        CompletableFuture<Void> newFuture = inputStreamAsync
                .thenAccept(is -> handler.accept(
                        JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject()))
                .exceptionally(e -> {
                    // FIXME: fix error handling correctly!
                    errorHandler.accept(e);
                    return null;
                });
        storeProcessFuture(newFuture);
    }

    // JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject()
    public void handleJsonObject(Consumer<JsonObject> handler) {
        handleJsonObject(handler, (ignored) -> {});
    }

    public void handleJsonArray(Consumer<JsonArray> handler, Consumer<Throwable> errorHandler) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        CompletableFuture<Void> newFuture = inputStreamAsync
                .thenAccept(is -> handler.accept(
                        JsonParser.parseReader(new InputStreamReader(is)).getAsJsonArray()))
                .exceptionally(e -> {
                    // FIXME: fix error handling correctly!
                    errorHandler.accept(e);
                    return null;
                });
        storeProcessFuture(newFuture);
    }

    public void handleJsonArray(Consumer<JsonArray> handler) {
        handleJsonArray(handler, (ignore) -> {});
    }

    private void storeProcessFuture(CompletableFuture<Void> newFuture) {
        CompletableFuture<Void> newFuture2 = newFuture.whenComplete((ignored, exc) -> {
            downloadFutures.remove(request);
        });
        processFutures.put(request, newFuture2);
    }
}
