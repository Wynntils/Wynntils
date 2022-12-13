/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.http.HttpRequest;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Response extends NetAction {
    public Response(HttpRequest request) {
        super(request);
    }

    // public static class FailureResponse extends Response {}

    private byte[] blob;

    public void handleJsonObject(Predicate<JsonObject> handler) {
        //        InputStreamReader stInputReader = new InputStreamReader(st.getInputStream(), StandardCharsets.UTF_8);
        //        JsonObject jsonObject = JsonParser.parseReader(stInputReader).getAsJsonObject();

    }

    public void handleJsonArray(Predicate<JsonArray> handler) {}

    public void handleBytes(Predicate<byte[]> handler) {}

    public void handleJsonArray(Predicate<JsonArray> handler, Consumer<Void> errorHandler) {}

    public void onError(Runnable handler) {}
}
