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
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wynntils.core.WynntilsMod;
import com.wynntils.services.mapdata.impl.MapIconImpl;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;

public final class JsonIconSerializer implements JsonDeserializer<MapIconImpl>, JsonSerializer<MapIconImpl> {
    @Override
    public MapIconImpl deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String id = json.get("id").getAsString();
        String base64Texture = json.get("texture").getAsString();

        // Checked before decoding, so that a huge texture is never even decoded
        if (base64Texture.length() > MapIconImpl.MAX_BASE64_LENGTH) {
            WynntilsMod.warn("Ignoring icon " + id + ", its texture is too large");
            return null;
        }

        try {
            byte[] texture = Base64.getDecoder().decode(base64Texture);
            return new MapIconImpl(id, texture);
        } catch (IOException | IllegalArgumentException e) {
            WynntilsMod.warn("Ignoring icon " + id + ", it has a bad texture: " + e.getMessage());
            return null;
        }
    }

    @Override
    public JsonElement serialize(MapIconImpl mapIcon, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("id", mapIcon.getIconId());
        json.addProperty("texture", Base64.getEncoder().encodeToString(mapIcon.getTextureBytes()));
        return json;
    }
}
