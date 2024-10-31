/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.google.gson.JsonObject;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import java.util.Map;

public class LiaoBotTranslationProvider extends OpenAITranslationProvider {
    @Override
    protected ApiResponse callApi(JsonObject requestBody, Map<String, String> headers) {
        return Managers.Net.callApi(UrlId.API_LIAOBOT_TRANSLATION, requestBody, headers);
    }
}
