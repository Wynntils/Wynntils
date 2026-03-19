/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A TranslationProvider that uses the DeepL Free API.
 * Requires a free API key from https://www.deepl.com/pro-api
 * Free tier allows up to 500,000 characters per month.
 * For Traditional Chinese, use language code "ZH-HANT".
 */
public class DeepLTranslationProvider extends CachingTranslationProvider {
    private static final String DEEPL_FREE_API_URL = "https://api-free.deepl.com/v2/translate";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static String apiKey = "";

    public static void setApiKey(String key) {
        apiKey = key == null ? "" : key.trim();
    }

    @Override
    protected String getCacheNamespace() {
        return "deepl";
    }

    @Override
    protected void translateNew(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
        if (apiKey == null || apiKey.isBlank()) {
            WynntilsMod.warn("DeepL API key is not set. Please set it in the Translation feature settings.");
            handleTranslation.accept(List.of());
            return;
        }

        JsonObject body = new JsonObject();
        JsonArray textArray = new JsonArray();
        messageList.forEach(textArray::add);
        body.add("text", textArray);
        body.addProperty("target_lang", toLanguage.toUpperCase());
        body.addProperty("source_lang", "EN");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPL_FREE_API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "DeepL-Auth-Key " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 403) {
                            WynntilsMod.warn("DeepL API: Invalid API key (403 Forbidden).");
                            handleTranslation.accept(List.of());
                            return;
                        }
                        if (response.statusCode() == 456) {
                            WynntilsMod.warn("DeepL API: Monthly character limit exceeded (456).");
                            handleTranslation.accept(List.of());
                            return;
                        }
                        if (response.statusCode() != 200) {
                            WynntilsMod.warn("DeepL API returned unexpected status: " + response.statusCode());
                            handleTranslation.accept(List.of());
                            return;
                        }

                        JsonObject json = WynntilsMod.GSON.fromJson(response.body(), JsonObject.class);
                        JsonArray translations = json.getAsJsonArray("translations");
                        List<String> result = new ArrayList<>();
                        for (var elem : translations) {
                            result.add(elem.getAsJsonObject().get("text").getAsString());
                        }

                        saveTranslation(toLanguage, messageList, result);
                        handleTranslation.accept(result);
                    } catch (Exception e) {
                        WynntilsMod.error("Failed to parse DeepL API response.", e);
                        handleTranslation.accept(List.of());
                    }
                })
                .exceptionally(e -> {
                    WynntilsMod.error("DeepL API request failed.", e);
                    handleTranslation.accept(List.of());
                    return null;
                });
    }
}
