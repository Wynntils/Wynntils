/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
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
import net.minecraft.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class NetManager {
    private static final int REQUEST_TIMEOUT_MILLIS = 10000;
    private static final File RESOURCE_ROOT = WynntilsMod.getModStorageDir("net-resources");
    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());
    private final ExecutorService pool = Executors.newFixedThreadPool(
            4,
            new ThreadFactoryBuilder()
                    .setNameFormat("wynntils-web-request-pool-%d")
                    .build());

    public static Response callApi(UrlId urlId, Map<String, String> arguments) {
        UrlManager.UrlInfo urlInfo = UrlManager.getUrlInfo(urlId);
        if (urlInfo.method() == UrlManager.Method.GET) {
            URI uri = URI.create(UrlManager.buildUrl(urlId, arguments));
            byte[] blob = getToMemory(uri);
            return new Response(null);
        } else {
            assert (urlInfo.method() == UrlManager.Method.POST);

            JsonObject jsonArgs = new JsonObject();
            arguments.entrySet().stream().forEach(entry -> {
                jsonArgs.addProperty(entry.getKey(), entry.getValue());
            });
            URI uri = URI.create(urlInfo.url());
            byte[] blob = postToMemory(uri, jsonArgs);
            return new Response(null);
        }
    }

    public static Response callApi(UrlId urlId) {
        return callApi(urlId, Map.of());
    }

    // THIS SHOULD ALSO BE CALLED FROM UpdateManager.tryFetchNewUpdate
    public static Download download(URI uri, String localFileName, String expectedHash, String id) {
        File localFile = new File(RESOURCE_ROOT, localFileName);
        if (!checkLocalHash(localFile, expectedHash)) {
            downloadToLocal(uri, localFile);
        }
        return new Download(localFile);
    }

    public static Download download(UrlId urlId) {
        URI uri = URI.create(UrlManager.getUrl(urlId));
        File localFile = new File(RESOURCE_ROOT, urlId.getId());
        downloadToLocal(uri, localFile);
        return new Download(localFile);
    }

    public static void openLink(UrlId urlId, Map<String, String> arguments) {
        URI uri = URI.create(UrlManager.buildUrl(urlId, arguments));
        openLink(uri);
    }

    /**
     * Open the specified URL in the user's browser.
     * @param url The url to open
     */
    public static void openLink(URI url) {
        Util.getPlatform().openUri(url);
    }

    private static byte[] postToMemory(URI uri, JsonObject arguments) {
        // FIXME: implement
        return null;
    }

    private static byte[] getToMemory(URI uri) {
        /*
        URL url = new URL("test");
        String fileName = "";
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);



        BufferedInputStream in = new BufferedInputStream(new URL(uri.toString()).openStream());

         */
        // FIXME: implement
        return null;
    }

    private static boolean checkLocalHash(File localFile, String expectedHash) {
        // FIXME: implement
        return false;
    }

    private static void downloadToLocal(URI uri, File localFile) {
        // FIXME: implement
    }

    private HttpURLConnection establishConnection(String url, Map<String, String> headers, int timeout)
            throws IOException {
        HttpURLConnection st = (HttpURLConnection) new URL(url).openConnection();
        st.setRequestProperty("User-Agent", USER_AGENT);
        if (!headers.isEmpty()) headers.forEach(st::addRequestProperty);

        st.setConnectTimeout(timeout);
        st.setReadTimeout(timeout);
        return st;
    }

    private HttpURLConnection establishPostConnection(
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
    private ThrowingConsumer<HttpURLConnection, IOException> postJsonElement(JsonElement element) {
        return writerThatPostBytes(element.toString().getBytes(StandardCharsets.UTF_8), "application/json");
    }

    private boolean cacheMD5Validator(byte[] data, String expectedHash, String id) {
        if (!MD5Verification.isMd5Digest(expectedHash)) return true;
        return cacheMD5Validator(data, () -> expectedHash, id);
    }

    /** As {@link #cacheMD5Validator(String)}, but lazily get the hash (inside a thread). */
    private boolean cacheMD5Validator(byte[] data, Supplier<String> expectedHashSupplier, String id) {
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
