/*
 * Copyright © Wynntils 2018-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A TranslationService that uses OpenAI's translation API.
 * This service is useful for generating natural language translations for game dialogues and texts.
 */
public class OpenAITranslationProvider extends CachingTranslationProvider {
    @Override
    protected void translateNew(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
        if (toLanguage == null || toLanguage.isEmpty()) {
            handleTranslation.accept(List.copyOf(messageList));
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo"); // 模型名称
        requestBody.put("temperature", "0.7");
        requestBody.put("stream", "false");

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty(
                "content",
                "You are a language model designed to provide translations for the MMORPG game Wynncraft. Translate dialogue, quest descriptions, and item names in a way that fits a medieval fantasy RPG setting. Maintain a tone that is immersive and consistent with the game's lore and atmosphere.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", String.join("\n", messageList));
        messages.add(userMessage);

        requestBody.put("messages", messages.toString());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + "API_KEY");
        headers.put("Content-Type", "application/json");

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_OPENAI_TRANSLATION, requestBody, headers);
        apiResponse.handleJsonObject(
                json -> {
                    String translatedMessage = json.getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .get("message")
                            .getAsJsonObject()
                            .get("content")
                            .getAsString();

                    List<String> result =
                            Arrays.stream(translatedMessage.split("\\n")).toList();
                    saveTranslation(toLanguage, messageList, result);
                    handleTranslation.accept(result);
                },
                onError -> handleTranslation.accept(List.copyOf(messageList)));
    }
}
