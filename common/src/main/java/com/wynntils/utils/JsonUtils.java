/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonUtils {
    public static String getNullableJsonString(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) return null;
        if (jsonElement.isJsonNull()) return null;

        return jsonElement.getAsString();
    }
}
