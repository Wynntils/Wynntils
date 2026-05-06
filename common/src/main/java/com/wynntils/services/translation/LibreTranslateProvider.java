/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.TaskUtils;
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
 * A TranslationProvider that uses a LibreTranslate instance.
 * LibreTranslate is a free, open-source machine translation API.
 * Self-hosting instructions: https://github.com/LibreTranslate/LibreTranslate
 *
 * Language codes follow ISO 639-1 format (e.g. "zh", "ja", "ko", "fr").
 */
public class LibreTranslateProvider extends CachingTranslationProvider {
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:5000";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static String baseUrl = DEFAULT_BASE_URL;
    private static String apiKey = "";

    public static void setBaseUrl(String url) {
        baseUrl = sanitizeBaseUrl(url);
    }

    public static void setApiKey(String key) {
        apiKey = key == null ? "" : key.trim();
    }

    @Override
    protected String getCacheNamespace() {
        return "libretranslate";
    }

    @Override
    protected void translateNew(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
        if (toLanguage == null || toLanguage.isBlank()) {
            handleTranslation.accept(List.copyOf(messageList));
            return;
        }

        TaskUtils.runAsync(() -> {
            List<String> results = new ArrayList<>();

            for (String message : messageList) {
                String translated = translateSingle(message, toLanguage);
                if (translated == null) {
                    handleTranslation.accept(List.of());
                    return;
                }
                results.add(translated);
            }

            saveTranslation(toLanguage, messageList, results);
            handleTranslation.accept(results);
        });
    }

    private String translateSingle(String message, String toLanguage) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("q", message);
            body.addProperty("source", "en");
            body.addProperty("target", toLanguage);
            body.addProperty("format", "text");
            if (!apiKey.isBlank()) {
                body.addProperty("api_key", apiKey);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sanitizeBaseUrl(baseUrl) + "/translate"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                WynntilsMod.warn("LibreTranslate API returned unexpected status: " + response.statusCode());
                return null;
            }

            JsonObject json = WynntilsMod.GSON.fromJson(response.body(), JsonObject.class);
            if (json == null || !json.has("translatedText")) {
                WynntilsMod.warn("LibreTranslate API returned no translatedText field.");
                return null;
            }

            return json.get("translatedText").getAsString();
        } catch (Exception e) {
            WynntilsMod.error("LibreTranslate API request failed.", e);
            return null;
        }
    }

    private static String sanitizeBaseUrl(String url) {
        String sanitized = url == null || url.isBlank() ? DEFAULT_BASE_URL : url.trim();
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }
}
