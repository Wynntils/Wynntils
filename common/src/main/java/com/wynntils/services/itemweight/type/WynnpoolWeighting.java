/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemweight.type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public record WynnpoolWeighting(ItemWeighting itemWeighting) {
    public static class WynnpoolWeightingDeserializer implements JsonDeserializer<WynnpoolWeighting> {
        @Override
        public WynnpoolWeighting deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String weightName = jsonObject.get("weight_name").getAsString();
            JsonObject identificationsObj = jsonObject.getAsJsonObject("identifications");

            Map<String, Double> identifications = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : identificationsObj.entrySet()) {
                identifications.put(entry.getKey(), entry.getValue().getAsDouble());
            }

            return new WynnpoolWeighting(new ItemWeighting(weightName, identifications));
        }
    }
}
