/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.downloader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.net.Reference;
import com.wynntils.utils.MD5Verification;
import com.wynntils.utils.ThrowingConsumer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Downloader {
    private final ExecutorService pool = Executors.newFixedThreadPool(
            4,
            new ThreadFactoryBuilder()
                    .setNameFormat("wynntils-web-request-pool-%d")
                    .build());

    public static DownloadableResource download(URI uri, File localFile, String id) {
        downloadToLocal(uri, localFile);
        return new DownloadableResource(localFile);
    }

    public static DownloadableResource download(String uri, File localFile, String id) {
        return download(URI.create(uri), localFile, id);
    }

    public static DownloadableResource downloadMd5(URI uri, File localFile, String expectedHash, String id) {
        if (!checkLocalHash(localFile, expectedHash)) {
            downloadToLocal(uri, localFile);
        }
        return new DownloadableResource(localFile);
    }

    public static DownloadableResource downloadMd5(String uri, File localFile, String expectedHash, String id) {
        return downloadMd5(URI.create(uri), localFile, expectedHash, id);
    }

    private static boolean checkLocalHash(File localFile, String expectedHash) {
        // FIXME: implement
        return false;
    }

    private static void downloadToLocal(URI uri, File localFile) {
        // FIXME: implement
    }

    public HttpURLConnection establishConnection(String url, Map<String, String> headers, int timeout)
            throws IOException {
        HttpURLConnection st = (HttpURLConnection) new URL(url).openConnection();
        st.setRequestProperty("User-Agent", Reference.getUserAgent());
        if (!headers.isEmpty()) headers.forEach(st::addRequestProperty);

        st.setConnectTimeout(timeout);
        st.setReadTimeout(timeout);
        return st;
    }

    public HttpURLConnection establishPostConnection(
            ThrowingConsumer<HttpURLConnection, IOException> writer,
            String url,
            Map<String, String> headers,
            int timeout)
            throws IOException {
        HttpURLConnection st = establishConnection(url, headers, timeout);
        st.setDoOutput(true);
        st.setRequestMethod("POST");
        writer.accept(st);
        return st;
    }

    /** Sets the writer to one that just writes the given bytes */
    private ThrowingConsumer<HttpURLConnection, IOException> writerThatPostBytes(byte[] data, String contentType) {
        return conn -> {
            conn.addRequestProperty("Content-Type", contentType);
            conn.addRequestProperty("Content-Length", Integer.toString(data.length));
            OutputStream o = conn.getOutputStream();
            o.write(data);
            o.flush();
        };
    }

    /** Sets the writer to a json string from a json element */
    public ThrowingConsumer<HttpURLConnection, IOException> postJsonElement(JsonElement element) {
        return writerThatPostBytes(element.toString().getBytes(StandardCharsets.UTF_8), "application/json");
    }

    public boolean cacheMD5Validator(byte[] data, String expectedHash, String id) {
        if (!MD5Verification.isMd5Digest(expectedHash)) return true;
        return cacheMD5Validator(data, () -> expectedHash, id);
    }

    /** As {@link #cacheMD5Validator(String)}, but lazily get the hash (inside a thread). */
    public boolean cacheMD5Validator(byte[] data, Supplier<String> expectedHashSupplier, String id) {
        String expectedHash = expectedHashSupplier.get();
        if (!MD5Verification.isMd5Digest(expectedHash)) return false;
        MD5Verification verification = new MD5Verification(data);
        if (verification.getMd5() == null) return false;
        boolean passed = verification.equalsHashString(expectedHash);
        if (!passed) {
            // TODO
            WynntilsMod.warn(id + ": MD5 verification failed. Expected: \"" + expectedHash + "\"; Got: \""
                    + verification.getMd5() + "\"");
        }
        return passed;
    }

    private void handleCache(String id, File cacheFile) {
        try {
            FileUtils.readFileToByteArray(cacheFile);
            boolean checkFailure = false;
            if (checkFailure) {
                WynntilsMod.warn("Error occurred whilst trying to use cache for " + id + " at " + cacheFile.getPath()
                        + ": Cache file is invalid");
                FileUtils.deleteQuietly(cacheFile);
                // req.onError();
            }
        } catch (FileNotFoundException ignore) {
            WynntilsMod.warn("Could not find file while trying to use cache as backup");
            // req.onError();
        } catch (Exception e) {
            WynntilsMod.warn("Error occurred whilst trying to use cache for " + id + " at " + cacheFile.getPath(), e);
            FileUtils.deleteQuietly(cacheFile);
            // req.onError();
        }
    }

    private boolean handleHttpConnection(String id, String url, File cacheFile) {
        HttpURLConnection st;
        try {
            st = establishConnection(url, null, 0);
            st.setReadTimeout(0);
            if (st.getResponseCode() != 200) {
                WynntilsMod.warn("Invalid response code for request");
                st.disconnect();
                return false;
            }
        } catch (Exception e) {
            WynntilsMod.warn("Error occurred whilst fetching " + id + " from " + url, e);
            return false;
        }

        try {
            byte[] data = IOUtils.toByteArray(st.getInputStream());
            try {
                FileUtils.writeByteArrayToFile(cacheFile, data);
            } catch (Exception e) {
                WynntilsMod.warn("Error occurred whilst writing cache for " + id, e);
                FileUtils.deleteQuietly(cacheFile);
            }
        } catch (IOException e) {
            WynntilsMod.warn("Error occurred whilst fetching " + id + " from " + url + ": "
                    + (e instanceof SocketTimeoutException ? "Socket timeout (server may be down)" : e.getMessage()));
        } catch (RuntimeException e) {
            WynntilsMod.warn("Error occurred whilst fetching " + id + " from " + url, e);
        }

        return false;
    }
}
