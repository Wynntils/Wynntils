/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.api;

import com.google.gson.JsonObject;
import java.net.URI;

public class ApiRequester {
    public static RequestResponse get(URI uri, String id) {
        byte[] blob = getToMemory(uri);
        return new RequestResponse(blob);
    }

    public static RequestResponse get(String uri, String id) {
        return get(URI.create(uri), id);
    }

    public static RequestResponse post(URI uri, JsonObject arguments, String id) {
        byte[] blob = postToMemory(uri, arguments);
        return new RequestResponse(blob);
    }

    public static RequestResponse post(String uri, JsonObject arguments, String id) {
        return post(URI.create(uri), arguments, id);
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
