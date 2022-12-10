/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonObject;
import java.net.URI;
import java.util.Map;

public class NetManager {
    private static final int REQUEST_TIMEOUT_MILLIS = 10000;

    public static ApiRequestResponse callApi(String urlId, Map<String, String> arguments) {
        if (UrlManager.getMethod(urlId).equals("post")) {
            JsonObject jsonArgs = new JsonObject();
            arguments.entrySet().stream().forEach(entry -> {
                jsonArgs.addProperty(entry.getKey(), entry.getValue());
            });
            URI uri = URI.create(UrlManager.getUrl(urlId));
            byte[] blob = postToMemory(uri, jsonArgs);
            return new ApiRequestResponse(blob);
        } else {
            URI uri = URI.create(UrlManager.buildUrl(urlId, arguments));
            byte[] blob = getToMemory(uri);
            return new ApiRequestResponse(blob);
        }
    }

    public static ApiRequestResponse callApi(String urlId) {
        return callApi(urlId, Map.of());
    }

    private static byte[] postToMemory(URI uri, JsonObject arguments) {
        // FIXME: implement
        return null;
    }

    private static byte[] getToMemory(URI uri) {
        // FIXME: implement
        return null;
    }
}
