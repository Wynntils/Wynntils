/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.json.JsonManager;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JsonProviderLoader {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(MapFeatureCategory.class, new CategoryDeserializer())
            .registerTypeHierarchyAdapter(MapFeatureIcon.class, new IconDeserializer())
            .registerTypeHierarchyAdapter(MapFeature.class, new FeatureDeserializer())
            .registerTypeHierarchyAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
            .registerTypeAdapterFactory(new JsonManager.EnumTypeAdapterFactory())
            .create();
    private JsonProvider provider;

    public JsonProviderLoader(String filename) {
        loadLocalResource(filename);
    }

    public JsonProvider getProvider() {
        return provider;
    }

    private void loadLocalResource(String filename) {
        try (InputStream inputStream = WynntilsMod.getModResourceAsStream(filename);
                Reader targetReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            provider = GSON.fromJson(targetReader, JsonProvider.class);
            System.out.println("provider:" + provider);
        } catch (MalformedJsonException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class CategoryDeserializer implements JsonDeserializer<MapFeatureCategory> {
        @Override
        public MapFeatureCategory deserialize(
                JsonElement jsonElement, Type jsonType, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String id = json.get("id").getAsString();
            String name = JsonUtils.getNullableJsonString(json, "name");
            JsonElement attributesJson = json.get("attributes");
            MapFeatureAttributes attributes = GSON.fromJson(attributesJson, JsonAttributes.class);

            return new JsonCategory(id, name, attributes);
        }
    }

    private static final class IconDeserializer implements JsonDeserializer<MapFeatureIcon> {
        @Override
        public MapFeatureIcon deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String id = json.get("id").getAsString();
            String base64Texture = json.get("texture").getAsString();
            byte[] texture = Base64.getDecoder().decode(base64Texture);

            try {
                return new JsonIcon(id, texture);
            } catch (IOException e) {
                WynntilsMod.warn("Bad icon texture for " + id, e);
                return null;
            }
        }
    }

    private static final class FeatureDeserializer implements JsonDeserializer<MapFeature> {
        @Override
        public MapFeature deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String id = JsonUtils.getNullableJsonString(json, "id");
            String category = JsonUtils.getNullableJsonString(json, "category");
            JsonElement locationJson = json.get("location");
            Location location = GSON.fromJson(locationJson, Location.class);
            JsonElement attributesJson = json.get("attributes");
            MapFeatureAttributes attributes = GSON.fromJson(attributesJson, JsonAttributes.class);

            return new JsonMapLocation(id, category, attributes, location);
        }
    }
}
