/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

    private CompletableFuture<HttpResponse<InputStream>> getHttpResponseAsync() {
        CompletableFuture<HttpResponse<InputStream>> future = NetManager.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .whenComplete((ignored, exc) -> {
                    downloadFutures.remove(request);
                });
        downloadFutures.put(request, future);
        return future;
    }

    private CompletableFuture<InputStream> getInputStreamAsync() {
        return getHttpResponseAsync().thenApply(HttpResponse::body);
    }

    private void doHandle(Consumer<InputStream> onCompletion, Consumer<Throwable> onError) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        CompletableFuture<Void> newFuture = inputStreamAsync
                .thenAccept((is) -> onCompletion.accept(is))
                .exceptionally(e -> {
                    // FIXME: fix error handling correctly!
                    onError.accept(e);
                    return null;
                });
        storeProcessFuture(newFuture);
    }

    private void storeProcessFuture(CompletableFuture<Void> processFuture) {
        CompletableFuture<Void> newFuture = processFuture.whenComplete((ignored, exc) -> {
            processFutures.remove(request);
        });
        processFutures.put(request, newFuture);
    }

    public void handleInputStream(Consumer<InputStream> onCompletion, Consumer<Throwable> onError) {
        doHandle(onCompletion, onError);
    }

    public void handleReader(Consumer<Reader> onCompletion, Consumer<Throwable> onError) {
        handleInputStream(is -> onCompletion.accept(new InputStreamReader(is)), onError);
    }

    public void handleJsonObject(Consumer<JsonObject> handler, Consumer<Throwable> onError) {
        handleReader(reader -> handler.accept(JsonParser.parseReader(reader).getAsJsonObject()), onError);
    }

    public void handleJsonObject(Consumer<JsonObject> handler) {
        handleJsonObject(handler, onError -> {
            WynntilsMod.warn("Error while reading resource");
        });
    }

    public void handleJsonArray(Consumer<JsonArray> handler, Consumer<Throwable> onError) {
        handleReader(reader -> handler.accept(JsonParser.parseReader(reader).getAsJsonArray()), onError);
    }

    public void handleJsonArray(Consumer<JsonArray> handler) {
        handleJsonArray(handler, onError -> {
            WynntilsMod.warn("Error while reading resource");
        });
    }
}
