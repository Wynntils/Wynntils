/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.api;

import com.google.gson.JsonObject;
import com.wynntils.core.net.Reference;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class ApiRequester {
    private static final int REQUEST_TIMEOUT_MILLIS = 10000;

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

    public static URLConnection generateURLRequest(String url) throws IOException {
        URLConnection st = new URL(url).openConnection();
        st.setRequestProperty("User-Agent", Reference.getUserAgent());
        st.setConnectTimeout(REQUEST_TIMEOUT_MILLIS);
        st.setReadTimeout(REQUEST_TIMEOUT_MILLIS);

        return st;
    }

    public static URLConnection generateURLRequestWithWynnApiKey(String url) throws IOException {
        URLConnection st = new URL(url).openConnection();
        st.setRequestProperty("User-Agent", Reference.getUserAgent());
        String apiKey = Reference.getWynnApiKey();
        st.setRequestProperty("apikey", apiKey);
        st.setConnectTimeout(REQUEST_TIMEOUT_MILLIS);
        st.setReadTimeout(REQUEST_TIMEOUT_MILLIS);

        return st;
    }
}
