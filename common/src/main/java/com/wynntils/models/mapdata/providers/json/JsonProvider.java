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
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.Download;
import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapIcon;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.MapFeature;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class JsonProvider implements MapDataProvider {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(MapCategory.class, new CategoryDeserializer())
            .registerTypeHierarchyAdapter(MapFeature.class, new FeatureDeserializer())
            .registerTypeHierarchyAdapter(MapIcon.class, new IconDeserializer())
            .registerTypeHierarchyAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
            .registerTypeAdapterFactory(new EnumUtils.EnumTypeAdapterFactory<>())
            .create();

    private final List<MapFeature> features;
    private final List<MapCategory> categories;
    private final List<MapIcon> icons;

    private JsonProvider(List<MapFeature> features, List<MapCategory> categories, List<MapIcon> icons) {
        this.features = features;
        this.categories = categories;
        this.icons = icons;
    }

    public static JsonProvider loadLocalResource(String id, String filename) {
        try (InputStream inputStream = WynntilsMod.getModResourceAsStream(filename);
                Reader targetReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return GSON.fromJson(targetReader, JsonProvider.class);
        } catch (MalformedJsonException e) {
            McUtils.sendErrorToClient("Error parsing map data for '" + id + "'");
            WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
        } catch (IOException e) {
            McUtils.sendErrorToClient("Error reading map data for '" + id + "'");
            WynntilsMod.warn("Error reading map data for '" + id + "'", e);
        } catch (Throwable e) {
            // This is typically a NPE in GSON parsing
            McUtils.sendErrorToClient("Error parsing map data for '" + id + "'");
            WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
        }
        return null;
    }

    public static void loadOnlineResource(String id, String url, BiConsumer<String, MapDataProvider> registerCallback) {
        Download dl = Managers.Net.download(URI.create(url), id);
        dl.handleReader(
                reader -> {
                    try {
                        registerCallback.accept(id, GSON.fromJson(reader, JsonProvider.class));
                    } catch (Throwable e) {
                        // This is either a json parse error or a NPE in GSON parsing
                        McUtils.sendErrorToClient("Error parsing map data for '" + id + "'");
                        WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
                    }
                },
                onError -> {
                    McUtils.sendErrorToClient("Error downloading map data for '" + id + "'");
                    WynntilsMod.warn("Error occurred while downloading map data for '" + id + "'", onError);
                });
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return features.stream();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return categories.stream();
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return icons.stream();
    }

    private static final class CategoryDeserializer implements JsonDeserializer<MapCategory> {
        @Override
        public MapCategory deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String id = json.get("id").getAsString();
            String name = JsonUtils.getNullableJsonString(json, "name");
            JsonElement attributesJson = json.get("attributes");
            MapAttributes attributes = GSON.fromJson(attributesJson, JsonAttributes.class);

            return new JsonCategory(id, name, attributes);
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
            MapAttributes attributes = GSON.fromJson(attributesJson, JsonAttributes.class);

            return new JsonMapLocation(id, category, attributes, location);
        }
    }

    private static final class IconDeserializer implements JsonDeserializer<MapIcon> {
        @Override
        public MapIcon deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
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
}
