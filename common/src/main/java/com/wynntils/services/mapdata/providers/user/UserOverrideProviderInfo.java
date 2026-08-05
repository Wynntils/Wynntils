/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import java.lang.reflect.Type;

public record UserOverrideProviderInfo(
        String targetCategoryId, // null if overriding a feature
        String targetFeatureId, // null if overriding a category
        MapAttributesImpl attributes) {
    public UserOverrideProviderInfo {
        if (targetCategoryId == null && targetFeatureId == null) {
            throw new IllegalArgumentException("At least one target must be set");
        }
    }

    public boolean overridesFeature(String categoryId, String featureId) {
        if (targetFeatureId != null) {
            return targetFeatureId.equals(featureId);
        }

        return targetCategoryId != null && categoryId.startsWith(targetCategoryId);
    }

    public static class UserOverrideProviderInfoSerializer
            implements JsonSerializer<UserOverrideProviderInfo>, JsonDeserializer<UserOverrideProviderInfo> {
        @Override
        public JsonElement serialize(UserOverrideProviderInfo src, Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();

            if (src.targetCategoryId() != null) {
                obj.addProperty("targetCategoryId", src.targetCategoryId());
            }
            if (src.targetFeatureId() != null) {
                obj.addProperty("targetFeatureId", src.targetFeatureId());
            }
            if (src.attributes() != null) {
                // Serialize attributes using the existing MapAttributesImpl adapter
                JsonObject attrObj = context.serialize(src.attributes(), MapAttributesImpl.class)
                        .getAsJsonObject();
                // Remove any null values from the attributes JSON
                JsonObject cleanedAttributes = new JsonObject();
                attrObj.entrySet().forEach(entry -> {
                    if (!entry.getValue().isJsonNull()) {
                        cleanedAttributes.add(entry.getKey(), entry.getValue());
                    }
                });
                obj.add("attributes", cleanedAttributes);
            }

            return obj;
        }

        @Override
        public UserOverrideProviderInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            String targetCategoryId = null;
            if (obj.has("targetCategoryId") && !obj.get("targetCategoryId").isJsonNull()) {
                targetCategoryId = obj.get("targetCategoryId").getAsString();
            }

            String targetFeatureId = null;
            if (obj.has("targetFeatureId") && !obj.get("targetFeatureId").isJsonNull()) {
                targetFeatureId = obj.get("targetFeatureId").getAsString();
            }

            MapAttributesImpl attributes = null;
            if (obj.has("attributes") && !obj.get("attributes").isJsonNull()) {
                attributes = context.deserialize(obj.get("attributes"), MapAttributesImpl.class);
            }

            return new UserOverrideProviderInfo(targetCategoryId, targetFeatureId, attributes);
        }
    }
}
