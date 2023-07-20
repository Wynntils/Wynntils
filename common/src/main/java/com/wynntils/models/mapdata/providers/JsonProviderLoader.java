/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.attributes.MapFeatureVisibility;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.wynn.WynnUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class JsonProviderLoader {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(MapFeatureCategory.class, new CategoryDeserializer())
            .registerTypeHierarchyAdapter(MapFeatureIcon.class, new IconDeserializer())
            .registerTypeHierarchyAdapter(MapFeature.class, new FeatureDeserializer())
            .registerTypeHierarchyAdapter(MapFeatureAttributes.class, new AttributesDeserializer())
            .registerTypeHierarchyAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
            .create();
    JsonProvider provider;

    public JsonProviderLoader() {
        loadTest();
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

    public void loadTest() {
        loadLocalResource("mapdata.json");
    }

    private static final class CategoryDeserializer implements JsonDeserializer<MapFeatureCategory> {
        @Override
        public MapFeatureCategory deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            /*
                {
      "attributes": {
        "icon": "magicus:icons:demo",
        "iconColor": "#ffffff",
        "iconVisibility": "always",
        "label": "Local Demo",
        "labelColor": "#ffffff",
        "labelShadow": "outline",
        "labelVisibility": "always",
        "priority": 500
      },
      "id": "magicus:demo:local",
      "name": "magicus demo of local json"
    }

             */
            String id = json.get("id").getAsString();
            String name = JsonUtils.getNullableJsonString(json, "name");
            JsonElement attributesJson = json.get("attributes");
            MapFeatureAttributes attributes = GSON.fromJson(attributesJson, MapFeatureAttributes.class);

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
            int width = json.get("width").getAsInt();
            int height = json.get("height").getAsInt();

            return new JsonIcon(id, texture, width, height);
        }
    }

    private static final class FeatureDeserializer implements JsonDeserializer<MapFeature> {
        @Override
        public MapFeature deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String id = JsonUtils.getNullableJsonString(json, "id");
            String category = JsonUtils.getNullableJsonString(json, "displayName");
            JsonElement locationJson = json.get("location");
            Location location = GSON.fromJson(locationJson, Location.class);

            return new JsonMapLocation(id, category, location);
        }
    }

    private static final class AttributesDeserializer implements JsonDeserializer<MapFeatureAttributes> {
        @Override
        public MapFeatureAttributes deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            /*
        "icon": "magicus:icons:demo",
        "iconColor": "#ffffff",
        "iconVisibility": "always",
        "label": "Local Demo",
        "labelColor": "#ffffff",
        "labelShadow": "outline",
        "labelVisibility": "always",
        "priority": 500

             */
            String label = JsonUtils.getNullableJsonString(json, "label");
            String icon = JsonUtils.getNullableJsonString(json, "icon");
            int rawPriority = JsonUtils.getNullableJsonInt(json, "priority");
            int priority = MathUtils.clamp(rawPriority, 0, 1000);


            JsonElement labelColorJson = json.get("labelColor");
            CustomColor labelColor = GSON.fromJson(labelColorJson, CustomColor.class);

            JsonElement labelShadowJson = json.get("labelShadow");
            TextShadow labelShadow = GSON.fromJson(labelShadowJson, TextShadow.class);

            JsonElement labelVisibilityJson = json.get("labelVisibility");
            MapFeatureVisibility labelVisibility = GSON.fromJson(labelVisibilityJson, MapFeatureVisibility.class);

            JsonElement iconColorJson = json.get("iconColor");
            CustomColor iconColor = GSON.fromJson(iconColorJson, CustomColor.class);

            JsonElement iconVisibilityJson = json.get("iconVisibility");
            MapFeatureVisibility iconVisibility = GSON.fromJson(iconVisibilityJson, MapFeatureVisibility.class);

            return new JsonAttributes(label, icon, priority, labelColor, labelShadow, labelVisibility,
                    iconColor, iconVisibility);
        }
    }
}
