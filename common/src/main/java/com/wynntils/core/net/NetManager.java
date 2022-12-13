/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import net.minecraft.Util;
import org.apache.commons.codec.digest.DigestUtils;

public class NetManager extends CoreManager {
    protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final int REQUEST_TIMEOUT_MILLIS = 10000;
    private static final File CACHE_DIR = WynntilsMod.getModStorageDir("cache");
    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static void init() {}

    public static ApiResponse callApi(UrlId urlId, Map<String, String> arguments) {
        UrlManager.UrlInfo urlInfo = UrlManager.getUrlInfo(urlId);
        return createApiResponse(urlInfo, arguments);
    }

    public static ApiResponse callApi(UrlId urlId) {
        return callApi(urlId, Map.of());
    }

    public static Download download(URI uri, File file) {
        return new Download(file, createGetRequest(uri));
    }

    public static Download download(URI uri, File file, String expectedHash) {
        if (checkLocalHash(file, expectedHash)) {
            return new Download(file);
        }

        return download(uri, file);
    }

    public static Download download(URI uri, String localFileName) {
        File localFile = new File(CACHE_DIR, localFileName);
        return download(uri, localFile);
    }

    public static Download download(URI uri, String localFileName, String expectedHash) {
        File localFile = new File(CACHE_DIR, localFileName);
        return download(uri, localFile, expectedHash);
    }

    public static Download download(UrlId urlId) {
        UrlManager.UrlInfo urlInfo = UrlManager.getUrlInfo(urlId);
        URI uri = URI.create(urlInfo.url());
        String localFileName = urlId.getId();

        if (urlInfo.md5().isPresent()) {
            return download(uri, localFileName, urlInfo.md5().get());
        }
        return download(uri, localFileName);
    }

    public static File getCacheFile(String localFileName) {
        return new File(CACHE_DIR, localFileName);
    }

    public static void openLink(URI url) {
        Util.getPlatform().openUri(url);
    }

    public static void openLink(UrlId urlId, Map<String, String> arguments) {
        URI uri = URI.create(UrlManager.buildUrl(urlId, arguments));
        openLink(uri);
    }

    private static HttpRequest createGetRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .build();
    }

    private static HttpRequest createPostRequest(URI uri, JsonObject jsonArgs) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonArgs.toString()))
                .build();
    }

    private static ApiResponse createApiResponse(UrlManager.UrlInfo urlInfo, Map<String, String> arguments) {
        if (urlInfo.method() == UrlManager.Method.GET) {
            URI uri = URI.create(UrlManager.buildUrl(urlInfo, arguments));
            HttpRequest request = createGetRequest(uri);
            return new ApiResponse(request);
        } else {
            assert (urlInfo.method() == UrlManager.Method.POST);

            JsonObject jsonArgs = new JsonObject();
            arguments.forEach((key, value) -> jsonArgs.addProperty(key, value));

            URI uri = URI.create(urlInfo.url());
            HttpRequest request = createPostRequest(uri, jsonArgs);
            return new ApiResponse(request);
        }
    }

    private static boolean checkLocalHash(File localFile, String expectedHash) {
        if (!localFile.exists()) return false;

        try (InputStream is = Files.newInputStream(localFile.toPath())) {
            String fileHash = DigestUtils.md5Hex(is);
            return fileHash.equalsIgnoreCase(expectedHash);
        } catch (IOException e) {
            WynntilsMod.warn("Error when calculading md5 for " + localFile.getPath(), e);
            return false;
        }
    }
}
