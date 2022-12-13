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
import java.util.function.Predicate;

public class Response extends NetAction {
    public Response(HttpRequest request) {
        super(request);
    }

    private CompletableFuture<InputStream> getInputStreamAsync() {
        return NetManager11.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body);
    }

    public void handleJsonObject(Predicate<JsonObject> handler, Consumer<Void> errorHandler) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        inputStreamAsync.thenApply(is ->
                handler.test(JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject()));
    }

    public void handleJsonObject(Predicate<JsonObject> handler) {
        handleJsonObject(handler, (ignored) -> {});
    }

    public void handleJsonArray(Predicate<JsonArray> handler, Consumer<Void> errorHandler) {
        CompletableFuture<InputStream> inputStreamAsync = getInputStreamAsync();
        inputStreamAsync.thenApply(is ->
                handler.test(JsonParser.parseReader(new InputStreamReader(is)).getAsJsonArray()));
    }

    public void handleJsonArray(Predicate<JsonArray> handler) {
        handleJsonArray(handler, (ignore) -> {});
    }
}
