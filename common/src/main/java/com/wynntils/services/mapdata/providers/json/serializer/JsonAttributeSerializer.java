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
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.services.mapdata.attributes.impl.MapAreaAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import java.lang.reflect.Type;

public final class JsonAttributeSerializer
        implements JsonDeserializer<MapAttributesImpl>, JsonSerializer<MapAttributesImpl> {
    @Override
    public MapAttributesImpl deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject attributesJson = json.getAsJsonObject();
        MapAttributesImpl attributesObj = context.deserialize(json, MapAttributesImpl.class);

        // We might not want a specific implementation, for example for categories
        if (type.equals(MapAttributesImpl.class)) {
            return attributesObj;
        }

        Type locationType = new TypeToken<MapLocationAttributesImpl>() {}.getType();
        Type pathType = new TypeToken<MapPathAttributesImpl>() {}.getType();
        Type areaType = new TypeToken<MapAreaAttributesImpl>() {}.getType();

        if (type.equals(locationType)) {
            MapLocationAttributes.getUnsupportedAttributes().forEach(invalidAttribute -> {
                if (attributesJson.getAsJsonObject().has(invalidAttribute)) {
                    WynntilsMod.warn("Unsupported attribute set for location: " + invalidAttribute);
                }
            });

            return new MapLocationAttributesImpl(attributesObj);
        }

        if (type.equals(areaType)) {
            MapAreaAttributes.getUnsupportedAttributes().forEach(invalidAttribute -> {
                if (attributesJson.getAsJsonObject().has(invalidAttribute)) {
                    WynntilsMod.warn("Unsupported attribute set for area: " + invalidAttribute);
                }
            });

            return new MapAreaAttributesImpl(attributesObj);
        }

        if (type.equals(pathType)) {
            MapPathAttributes.getUnsupportedAttributes().forEach(invalidAttribute -> {
                if (attributesJson.getAsJsonObject().has(invalidAttribute)) {
                    WynntilsMod.warn("Unsupported attribute set for path: " + invalidAttribute);
                }
            });

            return new MapPathAttributesImpl(attributesObj);
        }

        throw new JsonParseException("Attribute type is not location, path or area");
    }

    @Override
    public JsonElement serialize(MapAttributesImpl mapAttributes, Type type, JsonSerializationContext context) {
        // For the base class, we just serialize the attributes as is
        if (type.equals(MapAttributesImpl.class)) {
            return Managers.Json.GSON.toJsonTree(mapAttributes);
        }

        // For the specific implementations, we need to serialize the attributes that make sense,
        // explicitly skipping null and unsupported attributes
        JsonObject attributesJson = Managers.Json.GSON
                .toJsonTree(mapAttributes, MapAttributesImpl.class)
                .getAsJsonObject();
        JsonObject result = new JsonObject();

        attributesJson.entrySet().forEach(entry -> {
            String attribute = entry.getKey();
            JsonElement value = entry.getValue();
            if (value != null) {
                result.add(attribute, value);
            }
        });

        if (type.equals(MapLocationAttributesImpl.class)) {
            MapLocationAttributes.getUnsupportedAttributes().forEach(result::remove);
        } else if (type.equals(MapPathAttributesImpl.class)) {
            MapPathAttributes.getUnsupportedAttributes().forEach(result::remove);
        } else if (type.equals(MapAreaAttributesImpl.class)) {
            MapAreaAttributes.getUnsupportedAttributes().forEach(result::remove);
        } else {
            throw new JsonParseException("Attribute type is not location, path or area, nor the base class");
        }

        return result;
    }
}
