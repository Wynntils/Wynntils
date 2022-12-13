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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ApiResponse extends NetAction {
    public ApiResponse(HttpRequest request) {
        super(request);
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
        storeNewFuture(newFuture);
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
        storeNewFuture(newFuture);
    }

    public void handleJsonArray(Consumer<JsonArray> handler) {
        handleJsonArray(handler, (ignore) -> {});
    }
}
