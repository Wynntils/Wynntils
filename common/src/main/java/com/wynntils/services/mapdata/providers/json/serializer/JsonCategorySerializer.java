/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.wynntils.core.components.Managers;
import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import com.wynntils.services.mapdata.impl.MapCategoryImpl;
import com.wynntils.utils.JsonUtils;
import java.lang.reflect.Type;

public final class JsonCategorySerializer implements JsonDeserializer<MapCategoryImpl> {
    @Override
    public MapCategoryImpl deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
            throws JsonSyntaxException {
        JsonObject json = jsonElement.getAsJsonObject();

        String id = json.get("id").getAsString();
        String name = JsonUtils.getNullableJsonString(json, "name");
        JsonElement attributesJson = json.get("attributes");
        MapAttributesImpl attributes =
                attributesJson == null ? null : Managers.Json.GSON.fromJson(attributesJson, MapAttributesImpl.class);

        return new MapCategoryImpl(id, name, attributes);
    }
}
