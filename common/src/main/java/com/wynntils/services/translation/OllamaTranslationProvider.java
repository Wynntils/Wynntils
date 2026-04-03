/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonArray;
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
 * A TranslationProvider that uses a local Ollama instance for translation.
 * Ollama must be installed and running: https://ollama.com
 * A multilingual model such as qwen3:4b or aya:8b is recommended.
 */
public class OllamaTranslationProvider extends CachingTranslationProvider {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:11434";
    private static final String DEFAULT_MODEL = "qwen3:4b";

    private static String baseUrl = DEFAULT_BASE_URL;
    private static String model = DEFAULT_MODEL;

    public static void setBaseUrl(String url) {
        baseUrl = sanitizeBaseUrl(url);
    }

    public static void setModel(String modelName) {
        model = modelName == null || modelName.isBlank() ? DEFAULT_MODEL : modelName.trim();
    }

    @Override
    protected String getCacheNamespace() {
        return "ollama:" + model;
    }

    @Override
    protected void translateNew(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
        if (toLanguage == null || toLanguage.isBlank()) {
            handleTranslation.accept(List.copyOf(messageList));
            return;
        }

        TaskUtils.runAsync(() -> {
            List<String> translatedMessages = new ArrayList<>();

            for (String message : messageList) {
                String translatedMessage = translateSingle(message, toLanguage);
                if (translatedMessage == null) {
                    handleTranslation.accept(List.of());
                    return;
                }
                translatedMessages.add(translatedMessage);
            }

            saveTranslation(toLanguage, messageList, translatedMessages);
            handleTranslation.accept(translatedMessages);
        });
    }

    private String translateSingle(String message, String toLanguage) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("stream", false);

            JsonArray messages = new JsonArray();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty(
                    "content",
                    "You are a translation engine. Translate the user's English text into the requested target language. "
                            + "Preserve placeholders like {§a}, [§1], and <§2> exactly. Return only the translated text.");
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", "Target language code: " + toLanguage + "\nText:\n" + message);
            messages.add(userMessage);

            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sanitizeBaseUrl(baseUrl) + "/api/chat"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                WynntilsMod.warn("Ollama API returned unexpected status: " + response.statusCode());
                return null;
            }

            JsonObject json = WynntilsMod.GSON.fromJson(response.body(), JsonObject.class);
            JsonObject responseMessage = json.getAsJsonObject("message");
            if (responseMessage == null || !responseMessage.has("content")) {
                WynntilsMod.warn("Ollama API returned no message content.");
                return null;
            }

            return responseMessage.get("content").getAsString().trim();
        } catch (Exception e) {
            WynntilsMod.error("Ollama API request failed.", e);
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
