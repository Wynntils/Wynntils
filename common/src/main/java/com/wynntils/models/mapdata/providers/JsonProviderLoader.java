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
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonProviderLoader {
    JsonProvider provider;

    public JsonProviderLoader() {
        loadTest();
    }

    private void loadLocalResource(String filename) {
        try (InputStream inputStream = WynntilsMod.getModResourceAsStream(filename);
                Reader targetReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(GearInfo.class, new GearInfoDeserializer(null))
                    .create();
            provider = gson.fromJson(targetReader, JsonProvider.class);

        } catch (MalformedJsonException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadTest() {
        loadLocalResource("mapdata.json");
    }

    private static final class GearInfoDeserializer implements JsonDeserializer<GearInfo> {
        private final List<GearMajorId> allMajorIds;

        private GearInfoDeserializer(List<GearMajorId> allMajorIds) {
            this.allMajorIds = allMajorIds;
        }

        @Override
        public GearInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            // Wynncraft API has two fields: name and displayName. The former is the "api name", and is
            // always present, the latter is only present if it differs from the api name.
            // We want to store this the other way around: We always want a displayName (as the "name"),
            // but if it the apiName is different, we want to store it separately
            String primaryName = json.get("name").getAsString();
            String secondaryName = JsonUtils.getNullableJsonString(json, "displayName");

            if (secondaryName == null) {
                String normalizedApiName = WynnUtils.normalizeBadString(primaryName);
                if (!normalizedApiName.equals(primaryName)) {
                    // Normalization removed a ֎ from the name. This means we want to
                    // treat the name as apiName and the normalized name as display name
                    secondaryName = normalizedApiName;
                }
            }

            String name;
            String apiName;
            if (secondaryName == null) {
                name = primaryName;
                apiName = null;
            } else {
                name = secondaryName;
                apiName = primaryName;
            }

            GearType type = null;
            if (type == null) {
                throw new RuntimeException("Invalid Wynncraft data: item has no gear type");
            }
            GearTier tier = GearTier.fromString(json.get("tier").getAsString());
            int powderSlots = JsonUtils.getNullableJsonInt(json, "sockets");

            return null;
        }
    }

    protected static class WynncraftGearInfoResponse {
        protected List<GearInfo> items;
    }
}
