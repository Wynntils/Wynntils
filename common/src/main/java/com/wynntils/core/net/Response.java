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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Response extends NetAction {
    public Response(HttpRequest request) {
        super(request);
    }

    private CompletableFuture<InputStream> getInputStreamAsync() {
        return NetManager11.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body);
    }

    public void handleJsonObject(Consumer<JsonObject> handler, Consumer<Throwable> errorHandler) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        CompletableFuture<Void> a = inputStreamAsync
                .thenAccept(is -> handler.accept(
                        JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject()))
                .exceptionally(e -> {
                    // FIXME: fix error handling correctly!
                    errorHandler.accept(e);
                    return null;
                });
    }

    // JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject()
    public void handleJsonObject(Consumer<JsonObject> handler) {
        handleJsonObject(handler, (ignored) -> {});
    }

    public void handleJsonArray(Consumer<JsonArray> handler, Consumer<Throwable> errorHandler) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        CompletableFuture<Void> a = inputStreamAsync
                .thenAccept(is -> handler.accept(
                        JsonParser.parseReader(new InputStreamReader(is)).getAsJsonArray()))
                .exceptionally(e -> {
                    // FIXME: fix error handling correctly!
                    errorHandler.accept(e);
                    return null;
                });
    }

    public void handleJsonArray(Consumer<JsonArray> handler) {
        handleJsonArray(handler, (ignore) -> {});
    }
}
