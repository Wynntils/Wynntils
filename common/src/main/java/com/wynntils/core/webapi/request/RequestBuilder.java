/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.request;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebReader;
import com.wynntils.utils.objects.MD5Verification;
import com.wynntils.utils.objects.ThrowingBiPredicate;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Helper class for building a {@link Request} */
public class RequestBuilder {
    final String url;
    final String id;
    int parallelGroup = 0;
    ThrowingBiPredicate<URLConnection, byte[], IOException> handler;
    Request.RequestErrorHandler onError;
    final Map<String, String> headers = new HashMap<>();
    File cacheFile;
    Predicate<byte[]> cacheValidator = null;
    boolean useCacheAsBackup;
    int timeout = 16000;

    public RequestBuilder(String url, String id) {
        this.url = url;
        this.id = id;
    }

    /**
     * Sets the parallel group. Requests in the same parallel group will be requested at the same
     * time. Greater parallel groups will be requested after smaller ones.
     */
    public RequestBuilder withParallelGroup(int group) {
        this.parallelGroup = group;
        return this;
    }

    /**
     * Callback called with raw bytes from request.
     *
     * <p>If it returns true, the data is marked as good (Will be cached if there is a cache file).
     * If false or throws, the data is marked as bad (Uses cache file if this was a request, deletes
     * cache file if cache was bad)
     */
    public RequestBuilder handle(Predicate<byte[]> handler) {
        return handle((conn, data) -> handler.test(data));
    }

    /**
     * Callback called with the raw bytes from request and the connection itself. Return value is
     * interpreted as in {{@link #handle(Predicate) handle}}. The connection will be `null` if
     * loading from cache
     */
    public RequestBuilder handle(ThrowingBiPredicate<URLConnection, byte[], IOException> handler) {
        this.handler = handler;
        return this;
    }

    /**
     * As {@link #handle(Predicate) handle}, but the data is converted into a String first using
     * Charset
     */
    public RequestBuilder handleString(Predicate<String> handler, Charset charset) {
        return handle(data -> handler.test(new String(data, charset)));
    }

    public RequestBuilder handleString(
            ThrowingBiPredicate<URLConnection, String, IOException> handler, Charset charset) {
        return handle((conn, data) -> handler.test(conn, new String(data, charset)));
    }

    /** As {@link #handle(Predicate) handle}, but the data is interpreted as UTF-8 */
    public RequestBuilder handleString(Predicate<String> handler) {
        return handleString(handler, StandardCharsets.UTF_8);
    }

    public RequestBuilder handleString(ThrowingBiPredicate<URLConnection, String, IOException> handler) {
        return handleString(handler, StandardCharsets.UTF_8);
    }

    /** As {@link #handle(Predicate) handle}, but the data is parsed as JSON */
    public RequestBuilder handleJson(Predicate<JsonElement> handler) {
        return handleString(s -> handler.test(JsonParser.parseString(s)));
    }

    public RequestBuilder handleJson(ThrowingBiPredicate<URLConnection, JsonElement, IOException> handler) {
        return handleString((conn, s) -> handler.test(conn, JsonParser.parseString(s)));
    }

    /**
     * As {@link #handle(Predicate) handle}, but the data is parsed as JSON and converted into an
     * Object
     */
    public RequestBuilder handleJsonObject(Predicate<JsonObject> handler) {
        return handleJson(j -> j.isJsonObject() && handler.test(j.getAsJsonObject()));
    }

    public RequestBuilder handleJsonObject(ThrowingBiPredicate<URLConnection, JsonObject, IOException> handler) {
        return handleJson((conn, j) -> j.isJsonObject() && handler.test(conn, j.getAsJsonObject()));
    }

    /**
     * As {@link #handle(Predicate) handle}, but the data is parsed as JSON and converted into an
     * Array
     */
    public RequestBuilder handleJsonArray(Predicate<JsonArray> handler) {
        return handleJson(j -> j.isJsonArray() && handler.test(j.getAsJsonArray()));
    }

    public RequestBuilder handleJsonArray(ThrowingBiPredicate<URLConnection, JsonArray, IOException> handler) {
        return handleJson((conn, j) -> j.isJsonArray() && handler.test(conn, j.getAsJsonArray()));
    }

    /**
     * As {@link #handle(Predicate) handle}, but the data is parsed by {@link
     * WebReader#fromString(String) WebReader}
     */
    public RequestBuilder handleWebReader(Predicate<WebReader> handler) {
        return handleString(s -> {
            WebReader reader = WebReader.fromString(s);
            if (reader == null) return false;
            return handler.test(reader);
        });
    }

    /**
     * Sets the cache file. Good data will be written here, and if there is no good data, it will be
     * read from here.
     */
    public RequestBuilder cacheTo(File f) {
        this.cacheFile = f;
        return this;
    }

    /**
     * Set a local cache validator.
     *
     * <p>When a validator is set, the cache file will be read from first. If the validator returns
     * true, the cache file is used first, and a web request probably won't be made If false, make a
     * web request as normal.
     */
    public RequestBuilder cacheValidator(Predicate<byte[]> validator) {
        this.cacheValidator = data -> {
            try {
                return validator.test(data);
            } catch (Exception e) {
                WynntilsMod.warn("Unable to validate cache");
                e.printStackTrace();
                return false;
            }
        };
        return this;
    }

    /**
     * Allows using the cache as back up if no result has been achieved from either the cache
     * validator if set or the connection.
     *
     * <p>Naturally, if {@link RequestHandler#CACHE_ONLY} is set to true, then the connection does
     * not yield a result
     */
    public RequestBuilder useCacheAsBackup() {
        this.useCacheAsBackup = true;
        return this;
    }

    /** A MD5 hash cache validator. */
    public RequestBuilder cacheMD5Validator(String expectedHash) {
        if (!MD5Verification.isMd5Digest(expectedHash)) return this;
        return cacheMD5Validator(() -> expectedHash);
    }

    /** As {@link #cacheMD5Validator(String)}, but lazily get the hash (inside of a thread). */
    public RequestBuilder cacheMD5Validator(Supplier<String> expectedHashSupplier) {
        return cacheValidator(data -> {
            String expectedHash = expectedHashSupplier.get();
            if (!MD5Verification.isMd5Digest(expectedHash)) return false;
            MD5Verification verification = new MD5Verification(data);
            if (verification.getMd5() == null) return false;
            boolean passed = verification.equals(expectedHash);
            if (!passed) {
                // TODO
                WynntilsMod.warn(this.id + ": MD5 verification failed. Expected: \"" + expectedHash + "\"; Got: \""
                        + verification.getMd5() + "\"");
            }
            return passed;
        });
    }

    /** Called whenever a result could not be loaded */
    public RequestBuilder onError(Request.RequestErrorHandler onError) {
        this.onError = onError;
        return this;
    }

    /** Timeout length for requests */
    public RequestBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Adds a header to the request
     *
     * @param key the header key
     * @param value the header value
     */
    public RequestBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Request build() {
        if (cacheValidator != null && cacheFile == null)
            throw new IllegalStateException("Invalid cache file and validator pairing");

        return new Request(
                url,
                id,
                parallelGroup,
                handler,
                useCacheAsBackup,
                onError,
                headers,
                cacheFile,
                cacheValidator,
                timeout);
    }
}
