/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.features.utilities.TranslationFeature;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * A TranslationService that uses OpenAI's translation API.
 * This service generates natural language translations for game dialogues and texts.
 */
public class OpenAITranslationProvider extends CachingTranslationProvider {
    // Initialize the logger using WynntilsMod's logger
    private static final Logger LOGGER = WynntilsMod.getLogger();

    // Template for the system message with a placeholder for the target language
    private static final String SYSTEM_MESSAGE_TEMPLATE =
            """
                    You are a language model assigned to provide translations for a fantasy MMORPG Minecraft game called Wynncraft. \
                    Please translate all provided game dialogues, quest descriptions, item names, chat bar prompts (including events and area discoveries), \
                    and any lore-related content into %s. Use a style suitable for a medieval fantasy MMORPG world inspired by games like RuneScape.

                    Ensure that the translations maintain the game's lore, context, and specific terminology, including but not limited to:
                    - Names of provinces, realms, locations, cities, and towns (e.g., Ragni, Detlas, Almuj, Nemract, Troms, Ternaves, Nesaak, Selchar, Rymek, Maltic, Elkurn, Bremminglar, Katoa Ranch, Lusuco, Sarnfic, Nivla Forest, Dernel, Maro Peaks, Llevigar, Cinfras, Olux, Thesead, Eltom, Rodoroc, Ahmsord, Thanos, Kandon-Beda, Bucie)
                    - Names of NPCs and entities (e.g., Eluzterp, Royal Adviser Carlos, KEEL-tron, Worthington)
                    - Names of regions and mobs
                    - Class names and roles (e.g., Archer, Assassin, Mage, Warrior, Shaman)
                    - Specific events, historical periods, and timeline references (e.g., Before Portal [BP], After Portal [AP], Corruption War, Decay)
                    - Item names, artifacts, and magical elements
                    - Any in-game terminology and proper nouns

                    Please **do not translate** specific names and proper nouns, especially the names of locations and NPCs listed above, unless they have established translations within the game's context.

                    Only output the translated content, keeping the structure, line breaks, and order of the original text. Do not add any extra words, explanations, or formatting.""";

    // Immutable base headers excluding the Authorization header
    private static final Map<String, String> BASE_HEADERS = Map.of("Content-Type", "application/json");

    private final TranslationFeature translationFeature = Managers.Feature.getFeatureInstance(TranslationFeature.class);

    @Override
    protected void translateNew(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
        // Guard Clause: Check if the target language is null or empty
        if (toLanguage == null || toLanguage.trim().isEmpty()) {
            handleTranslation.accept(List.copyOf(messageList));
            return;
        }

        // Guard Clause: Remove null or empty messages and check if the list is empty
        List<String> filteredMessageList = messageList.stream()
                .filter(message -> message != null && !message.trim().isEmpty())
                .collect(Collectors.toList());

        if (filteredMessageList.isEmpty()) {
            LOGGER.info("No valid messages to translate.");
            handleTranslation.accept(List.copyOf(messageList));
            return;
        }

        // Join the filtered message list into a single string using "{NL}" as the separator
        String message = String.join("{NL}", filteredMessageList);

        // Create the request body as a JsonObject
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", translationFeature.modelName.get());

        // Build the messages array
        JsonArray messages = new JsonArray();

        // Create the system message using the template
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", String.format(SYSTEM_MESSAGE_TEMPLATE, toLanguage));
        messages.add(systemMessage);

        // Create the user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        messages.add(userMessage);

        // Add the messages array to the request body
        requestBody.add("messages", messages);

        // Prepare headers by copying the base headers and adding the Authorization header
        Map<String, String> headers = new java.util.HashMap<>(BASE_HEADERS);
        headers.put("Authorization", "Bearer " + translationFeature.apiKey.get());

        // Log the request body for debugging
        LOGGER.debug("Request Body JSON: {}", requestBody);

        // Use the updated callApi method from NetManager
        ApiResponse apiResponse = this.callApi(requestBody, headers);

        // Handle the API response with guard clauses
        apiResponse.handleJsonObject(
                json -> {
                    LOGGER.debug("API Response JSON: {}", json.toString());

                    JsonArray choicesArray = json.getAsJsonArray("choices");

                    // Guard Clause: Check if choices array is present and not empty
                    if (choicesArray == null || choicesArray.isEmpty()) {
                        LOGGER.warn("Choices array missing or empty in API response.");
                        handleTranslation.accept(List.copyOf(messageList));
                        return;
                    }

                    JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
                    JsonObject messageObject = firstChoice.getAsJsonObject("message");

                    // Guard Clause: Check if message object and content are present
                    if (messageObject == null || !messageObject.has("content")) {
                        LOGGER.warn("Message object or content missing in API response.");
                        handleTranslation.accept(List.copyOf(messageList));
                        return;
                    }

                    String translatedMessage = messageObject.get("content").getAsString();
                    LOGGER.debug("Translated Message: {}", translatedMessage);

                    List<String> result =
                            Arrays.stream(translatedMessage.split("\\{NL\\}")).collect(Collectors.toList());

                    saveTranslation(toLanguage, filteredMessageList, result);
                    handleTranslation.accept(result);
                },
                onError -> {
                    LOGGER.error("API call error, returning original message list.", onError);
                    handleTranslation.accept(List.copyOf(messageList));
                });
    }

    /**
     * if u want set endpoint of OpenAI u can ovrride the method
     * @param requestBody
     * @param headers
     * @return ApiResponse
     */
    protected ApiResponse callApi(JsonObject requestBody, Map<String, String> headers) {
        return Managers.Net.callApi(UrlId.API_OPENAI_TRANSLATION, requestBody, headers);
    }
}
