/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public final class JsonUtils {
    public static String getNullableJsonString(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) return null;
        if (jsonElement.isJsonNull()) return null;

        return jsonElement.getAsString();
    }

    public static int getNullableJsonInt(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) return 0;
        if (jsonElement.isJsonNull()) return 0;

        return jsonElement.getAsInt();
    }

    public static boolean getNullableJsonBoolean(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) return false;
        if (jsonElement.isJsonNull()) return false;

        return jsonElement.getAsBoolean();
    }

    public static JsonObject getNullableJsonObject(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) return new JsonObject();
        if (jsonElement.isJsonNull()) return new JsonObject();

        return jsonElement.getAsJsonObject();
    }

    public static JsonArray getNullableJsonArray(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) return new JsonArray();
        if (jsonElement.isJsonNull()) return new JsonArray();

        return jsonElement.getAsJsonArray();
    }

    public static List<String> getStringOrStringArray(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        if (jsonElement.isJsonArray()) {
            JsonArray array = jsonElement.getAsJsonArray();
            for (JsonElement element : array) {
                if (!element.isJsonNull()) {
                    result.add(element.getAsString());
                }
            }
        } else {
            result.add(jsonElement.getAsString());
        }

        return result;
    }
}
