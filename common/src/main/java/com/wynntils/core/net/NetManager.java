/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
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

    public NetManager(UrlManager urlManager) {
        // NetManager is involved in a circular dependency with UrlManager. This means
        // it will be instantiated twice, first as a throw-away instance local to UrlManager
        // bootstrapping only, then as the real instance for Managers.
        super(List.of(urlManager));
    }

    public ApiResponse callApi(UrlId urlId, Map<String, String> arguments) {
        UrlManager.UrlInfo urlInfo = Managers.Url.getUrlInfo(urlId);
        return createApiResponse(urlId, urlInfo, arguments);
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
        return new Download(localFile.getName(), localFile, createGetRequest(uri), processedEvent);
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

    private HttpRequest createGetRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .build();
    }

    private HttpRequest createPostRequest(URI uri, JsonObject jsonArgs) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonArgs.toString()))
                .build();
    }

    private ApiResponse createApiResponse(UrlId urlId, UrlManager.UrlInfo urlInfo, Map<String, String> arguments) {
        if (urlInfo.method() == UrlManager.Method.GET) {
            URI uri = URI.create(Managers.Url.buildUrl(urlInfo, arguments));
            HttpRequest request = createGetRequest(uri);
            return new ApiResponse(urlId.toString(), request, new NetResultProcessedEvent.ForUrlId(urlId));
        } else {
            assert (urlInfo.method() == UrlManager.Method.POST);

            JsonObject jsonArgs = new JsonObject();
            arguments.forEach(jsonArgs::addProperty);

            URI uri = URI.create(urlInfo.url());
            HttpRequest request = createPostRequest(uri, jsonArgs);
            return new ApiResponse(urlId.toString(), request, new NetResultProcessedEvent.ForUrlId(urlId));
        }
    }

    private boolean checkLocalHash(File localFile, String expectedHash) {
        if (!localFile.exists()) return false;

        try (InputStream is = Files.newInputStream(localFile.toPath())) {
            String fileHash = DigestUtils.md5Hex(is);
            return fileHash.equalsIgnoreCase(expectedHash);
        } catch (IOException e) {
            WynntilsMod.warn("Error when calculating md5 for " + localFile.getPath(), e);
            return false;
        }
    }
}
