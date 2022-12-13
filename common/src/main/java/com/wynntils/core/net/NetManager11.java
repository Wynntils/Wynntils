/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.Util;
import org.apache.commons.codec.digest.DigestUtils;

public class NetManager11 {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

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

    private static HttpRequest getRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .build();
        return request;
    }

    private static HttpRequest postRequest(URI uri, String data) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MILLIS))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
        return request;
    }

    public static Response callApi(UrlId urlId, Map<String, String> arguments) {
        UrlManager.UrlInfo urlInfo = UrlManager.getUrlInfo(urlId);
        if (urlInfo.method() == UrlManager.Method.GET) {
            URI uri = URI.create(UrlManager.buildUrl(urlId, arguments));
            HttpRequest request = getRequest(uri);
            return new Response(request);
        } else {
            assert (urlInfo.method() == UrlManager.Method.POST);

            JsonObject jsonArgs = new JsonObject();
            arguments.entrySet().stream().forEach(entry -> {
                jsonArgs.addProperty(entry.getKey(), entry.getValue());
            });
            URI uri = URI.create(urlInfo.url());
            HttpRequest request = postRequest(uri, jsonArgs.toString());
            return new Response(request);
        }
    }

    public static Response callApi(UrlId urlId) {
        return callApi(urlId, Map.of());
    }

    public static Download download(URI uri, String localFileName, String expectedHash, String id) {
        File localFile = new File(RESOURCE_ROOT, localFileName);
        if (!checkLocalHash(localFile, expectedHash)) {
            downloadToLocal(uri, localFile);
        }
        return new Download(localFile);
    }

    private static boolean checkLocalHash(File localFile, String expectedHash) {
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

    public static Download download(UrlId urlId) {
        URI uri = URI.create(UrlManager.getUrl(urlId));
        File localFile = new File(RESOURCE_ROOT, urlId.getId());
        downloadToLocal(uri, localFile);
        return new Download(localFile);
    }

    private static void downloadToLocal(URI uri, File localFile) {}

    public void getSyncToString(String uri) throws Exception {
        HttpRequest request = getRequest(URI.create(uri));

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }

    public void getSyncToFile(String uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();

        HttpResponse<Path> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get("body.txt")));

        System.out.println("Response in file:" + response.body());
    }

    public CompletableFuture<String> getAsyncToString(String uri) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();

        // kan returnera inputstream, är antagligen bäst.

        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<Path> getAsyncToFile(String uri) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();

        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofFile(Paths.get("body.txt")))
                .thenApply(HttpResponse::body);
    }

    public void postWithJson(String uri, String data) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<?> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        System.out.println(response.statusCode());
    }

    public CompletableFuture<Void> postJSON(URI uri, Map<String, String> map) throws IOException {
        String requestBody = new Gson().toJson(map);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(System.out::println);
    }

    public CompletableFuture<JsonElement> JSONBodyAsMap(URI uri) {
        UncheckedObjectMapper objectMapper = new UncheckedObjectMapper();

        HttpRequest request =
                HttpRequest.newBuilder(uri).header("Accept", "application/json").build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(objectMapper::readValue);
    }

    class UncheckedObjectMapper {
        /**
         * Parses the given JSON string into a Map.
         */
        JsonElement readValue(String content) {
            try {
                return JsonParser.parseString(content);
            } catch (Exception ioe) {
                throw new CompletionException(ioe);
            }
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
