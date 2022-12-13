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
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final int REQUEST_TIMEOUT_MILLIS = 10000;
    private static final File RESOURCE_ROOT = WynntilsMod.getModStorageDir("cache");
    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static void init() {}

    private static HttpRequest getRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .build();
        return request;
    }

    private static HttpRequest postRequest(URI uri, JsonObject jsonArgs) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonArgs.toString()))
                .build();
        return request;
    }

    public static ApiResponse callApi(UrlId urlId, Map<String, String> arguments) {
        UrlManager.UrlInfo urlInfo = UrlManager.getUrlInfo(urlId);
        if (urlInfo.method() == UrlManager.Method.GET) {
            URI uri = URI.create(UrlManager.buildUrl(urlId, arguments));
            HttpRequest request = getRequest(uri);
            return new ApiResponse(request);
        } else {
            assert (urlInfo.method() == UrlManager.Method.POST);

            JsonObject jsonArgs = new JsonObject();
            arguments.entrySet().stream().forEach(entry -> {
                jsonArgs.addProperty(entry.getKey(), entry.getValue());
            });
            URI uri = URI.create(urlInfo.url());
            HttpRequest request = postRequest(uri, jsonArgs);
            return new ApiResponse(request);
        }
    }

    public static ApiResponse callApi(UrlId urlId) {
        return callApi(urlId, Map.of());
    }

    public static Download download(URI uri, File file) {
        return new Download(file, getRequest(uri));
    }

    public static Download download(URI uri, File file, String expectedHash) {
        if (checkLocalHash(file, expectedHash)) {
            return new Download(file);
        }
        return download(uri, file);
    }

    public static Download download(URI uri, String localFileName) {
        File localFile = new File(RESOURCE_ROOT, localFileName);
        return download(uri, localFile);
    }

    public static Download download(URI uri, String localFileName, String expectedHash) {
        File localFile = new File(RESOURCE_ROOT, localFileName);
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

    private static boolean checkLocalHash(File localFile, String expectedHash) {
        // Alternative solution: MD5Verification.isMd5Digest(expectedHash)
        if (!localFile.exists()) return false;
        try {
            try (InputStream is = Files.newInputStream(localFile.toPath())) {
                String fileHash = DigestUtils.md5Hex(is);
                return fileHash.equalsIgnoreCase(expectedHash);
            }
        } catch (IOException e) {
            WynntilsMod.warn("Error when calculading md5 for " + localFile.getPath(), e);
            return false;
        }
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
}
