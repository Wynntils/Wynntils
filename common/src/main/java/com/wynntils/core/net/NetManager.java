/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.event.NetResultProcessedEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.commons.codec.digest.DigestUtils;

public final class NetManager extends Manager {
    static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final int REQUEST_TIMEOUT_MILLIS = 10000;
    private static final File CACHE_DIR = WynntilsMod.getModStorageDir("cache");
    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s+MC-%s (%s) %s",
            WynntilsMod.getVersion(),
            SharedConstants.getCurrentVersion().getName(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public NetManager() {
        super(List.of());
    }

    public ApiResponse callApi(UrlId urlId, Map<String, String> arguments, Map<String, String> headers) {
        UrlManager.UrlInfo urlInfo = Managers.Url.getUrlInfo(urlId);
        return createApiResponse(urlId, urlInfo, arguments, headers);
    }

    public ApiResponse callApi(UrlId urlId, Map<String, String> arguments) {
        return callApi(urlId, arguments, Map.of());
    }

    public ApiResponse callApi(UrlId urlId) {
        return callApi(urlId, Map.of());
    }

    public Download download(URI uri, String localFileName) {
        File localFile = new File(CACHE_DIR, localFileName);
        return download(uri, localFile, new NetResultProcessedEvent.ForLocalFile(localFileName));
    }

    public Download download(URI uri, String localFileName, String expectedHash) {
        File localFile = new File(CACHE_DIR, localFileName);
        return download(uri, localFile, expectedHash, new NetResultProcessedEvent.ForLocalFile(localFileName));
    }

    /**
     * Download a file from the given URL and save it to the cache directory.
     * <p><b>This method should only be used when file needs to be downloaded dynamically.
     * If you want to download a file in a {@link com.wynntils.core.components.CoreComponent} and use it,
     * you may want to use {@link DownloadManager}'s download dependency system.</b></p>
     * @param urlId The url id to download from
     * @return The download object
     */
    public Download download(UrlId urlId) {
        UrlManager.UrlInfo urlInfo = Managers.Url.getUrlInfo(urlId);
        URI uri = URI.create(urlInfo.url());
        String localFileName = urlId.getId();
        File localFile = new File(CACHE_DIR, localFileName);

        if (urlInfo.md5().isPresent()) {
            return download(uri, localFile, urlInfo.md5().get(), new NetResultProcessedEvent.ForUrlId(urlId));
        }

        return download(uri, localFile, new NetResultProcessedEvent.ForUrlId(urlId));
    }

    private Download download(URI uri, File localFile, NetResultProcessedEvent processedEvent) {
        return new Download(localFile.getName(), localFile, createGetRequest(uri, Map.of()), processedEvent);
    }

    private Download download(URI uri, File localFile, String expectedHash, NetResultProcessedEvent processedEvent) {
        // For debugging, always return cached files if requested
        if (WynntilsMod.isDevelopmentEnvironment() && new File(CACHE_DIR, "keep").exists()) {
            return new Download(localFile.getName(), localFile, processedEvent);
        }

        if (checkLocalHash(localFile, expectedHash)) {
            return new Download(localFile.getName(), localFile, processedEvent);
        }

        return download(uri, localFile, processedEvent);
    }

    public File getCacheDir() {
        return CACHE_DIR;
    }

    public File getCacheFile(String localFileName) {
        return new File(CACHE_DIR, localFileName);
    }

    public void openLink(URI url) {
        Util.getPlatform().openUri(url);
    }

    public void openLink(UrlId urlId, Map<String, String> arguments) {
        URI uri = URI.create(Managers.Url.buildUrl(urlId, arguments));
        openLink(uri);
    }

    private HttpRequest createGetRequest(URI uri, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT);

        headers.forEach(builder::header);

        return builder.build();
    }

    private HttpRequest createPostRequest(URI uri, Map<String, String> headers, JsonObject jsonArgs) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonArgs.toString()));

        headers.forEach(builder::header);

        return builder.build();
    }

    private ApiResponse createApiResponse(
            UrlId urlId, UrlManager.UrlInfo urlInfo, Map<String, String> arguments, Map<String, String> headers) {
        if (urlInfo.method() == UrlManager.Method.GET) {
            URI uri = URI.create(Managers.Url.buildUrl(urlInfo, arguments));
            HttpRequest request = createGetRequest(uri, headers);
            return new ApiResponse(urlId.toString(), request, new NetResultProcessedEvent.ForUrlId(urlId));
        } else {
            assert (urlInfo.method() == UrlManager.Method.POST);
            assert (arguments.keySet().equals(new HashSet<>(urlInfo.arguments())))
                    : "Arguments mismatch for " + urlId + ", expected: " + urlInfo.arguments() + " got: "
                            + arguments.keySet();

            JsonObject jsonArgs = new JsonObject();
            arguments.forEach(jsonArgs::addProperty);

            URI uri = URI.create(urlInfo.url());
            HttpRequest request = createPostRequest(uri, headers, jsonArgs);
            return new ApiResponse(urlId.toString(), request, new NetResultProcessedEvent.ForUrlId(urlId));
        }
    }

    private boolean checkLocalHash(File localFile, String expectedHash) {
        if (!localFile.exists()) return false;

        try (InputStream is = Files.newInputStream(localFile.toPath())) {
            String fileHash = DigestUtils.md5Hex(is);
            boolean hashMatches = fileHash.equalsIgnoreCase(expectedHash);
            if (WynntilsMod.isDevelopmentEnvironment() && !hashMatches) {
                WynntilsMod.warn("Hash mismatch for " + localFile.getPath() + ": " + fileHash + " != " + expectedHash
                        + ". If you see this often, check urls.json, there might be an outdated hash.");
            }
            return hashMatches;
        } catch (IOException e) {
            WynntilsMod.warn("Error when calculating md5 for " + localFile.getPath(), e);
            return false;
        }
    }
}
