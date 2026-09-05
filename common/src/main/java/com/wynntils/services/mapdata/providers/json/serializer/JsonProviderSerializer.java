/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.wynntils.services.mapdata.features.impl.MapAreaImpl;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.providers.json.JsonFeatures;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.List;

public final class JsonProviderSerializer implements JsonDeserializer<JsonProvider> {
    @Override
    public JsonProvider deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonProvider = json.getAsJsonObject();

        int version = JsonUtils.getNullableJsonInt(jsonProvider, "version");
        if (version == 0) {
            throw new JsonParseException("Missing version field in json provider.");
        }
        if (version > 1) {
            throw new JsonParseException("Unsupported version in json provider.");
        }

        JsonObject featuresObject = JsonUtils.getNullableJsonObject(jsonProvider, "features");

        // Manually construct the JsonFeatures object, so we don't have empty lists
        JsonFeatures features = new JsonFeatures(
                context.deserialize(
                        JsonUtils.getNullableJsonArray(featuresObject, "locations"),
                        new TypeToken<List<MapLocationImpl>>() {}.getType()),
                context.deserialize(
                        JsonUtils.getNullableJsonArray(featuresObject, "areas"),
                        new TypeToken<List<MapAreaImpl>>() {}.getType()),
                context.deserialize(
                        JsonUtils.getNullableJsonArray(featuresObject, "paths"),
                        new TypeToken<List<MapPathImpl>>() {}.getType()));

        if (!features.validate()) {
            throw new JsonParseException("Invalid features in json provider.");
        }

        List<MapCategory> categories = context.deserialize(
                JsonUtils.getNullableJsonArray(jsonProvider, "categories"),
                new TypeToken<List<MapCategory>>() {}.getType());
        List<MapIcon> icons = context.deserialize(
                JsonUtils.getNullableJsonArray(jsonProvider, "icons"), new TypeToken<List<MapIcon>>() {}.getType());

        return new JsonProvider(version, features, categories, icons);
    }
}
