/*
 * Copyright © Wynntils 2018-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A TranslationService that uses the free googleapi translation API. This service is free but is severely
 * restricted. There is a rate limit of about 100 messages per hour and IP address. This is typically
 * sufficient for NPCs translation, but not for general chat messages, at least not in chatty areas like Detlas.
 */
public class GoogleApiTranslationProvider extends CachingTranslationProvider {
    @Override
    protected void translateNew(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
        if (toLanguage == null || toLanguage.isEmpty()) {
            handleTranslation.accept(List.copyOf(messageList));
            return;
        }

        String message = String.join("{NL}", messageList);
        Map<String, String> arguments = new HashMap<>();
        arguments.put("lang", toLanguage);
        arguments.put("text", message);

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_GOOGLE_TRANSLATION, arguments);
        apiResponse.handleJsonArray(
                json -> {
                    StringBuilder builder = new StringBuilder();
                    JsonArray array = json.get(0).getAsJsonArray();
                    for (JsonElement elem : array) {
                        String part = elem.getAsJsonArray().get(0).getAsString();
                        builder.append(part);
                    }
                    String translatedMessage = builder.toString();
                    List<String> result =
                            Arrays.stream(translatedMessage.split("\\{NL\\}")).toList();
                    saveTranslation(toLanguage, messageList, result);
                    handleTranslation.accept(result);
                },
                onError -> {
                    // If Google translate return no data ( 500 error ), display default lang
                    handleTranslation.accept(List.copyOf(messageList));
                });
    }
}
