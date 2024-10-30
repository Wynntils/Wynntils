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

        String message = String.join("{NL}", messageList);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4o-mini");

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty(
                "content",
                "You are a language model assigned to provide translations for a fantasy MMORPG Minecraft game called Wynncraft. Please translate all provided game dialogues, quest descriptions, item names, chat bar prompts (including events and area discoveries), and any lore-related content into " + toLanguage + ". Use a style suitable for a medieval fantasy MMORPG world inspired by games like RuneScape. Ensure that the translations maintain the game's lore, context, and specific terminology, including but not limited to:\n" +
                        "- Names of provinces, realms, and locations (e.g., Fruma, Wynn, Corkus, Gavel, Time Valley, Realm of Light, Silent Expanse)\n" +
                        "- Names of characters, NPCs, and entities (e.g., Bak'al, Bob, Lari, Dullahan, Theorick Twain)\n" +
                        "- Class names and roles (e.g., Archer, Assassin, Mage, Warrior, Shaman)\n" +
                        "- Specific events, historical periods, and timeline references (e.g., Before Portal [BP], After Portal [AP], Corruption War, Decay)\n" +
                        "- Item names, artifacts, and magical elements\n" +
                        "- Any in-game terminology and proper nouns\n\n" +
                        "Only output the translated content, keeping the structure, line breaks, and order of the original text. Do not add any extra words, explanations, or formatting. Avoid translating specific names and proper nouns unless they have established translations within the game's context."
        );
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + "API_KEY");
        headers.put("Content-Type", "application/json");

        ApiResponse apiResponse = Managers.Net.callApiWithJsonBody(UrlId.API_OPENAI_TRANSLATION, requestBody, headers);

        apiResponse.handleJsonObject(
                json -> {
                    System.out.println("API Response JSON: " + json.toString());

                    JsonArray choicesArray = json.getAsJsonArray("choices");
                    if (choicesArray != null && !choicesArray.isEmpty()) {
                        JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
                        JsonObject messageObject = firstChoice.getAsJsonObject("message");

                        if (messageObject != null && messageObject.has("content")) {
                            String translatedMessage =
                                    messageObject.get("content").getAsString();
                            System.out.println("Translated Message: " + translatedMessage);

                            List<String> result = Arrays.stream(translatedMessage.split("\\{NL\\}"))
                                    .toList();

                            saveTranslation(toLanguage, messageList, result);
                            handleTranslation.accept(result);
                        } else {
                            System.out.println("Message object or content missing in API response.");
                            handleTranslation.accept(List.copyOf(messageList));
                        }
                    } else {
                        System.out.println("Choices array missing or empty in API response.");
                        handleTranslation.accept(List.copyOf(messageList));
                    }
                },
                onError -> {
                    System.out.println("API call error, returning original message list.");
                    handleTranslation.accept(List.copyOf(messageList));
                });
    }
}
