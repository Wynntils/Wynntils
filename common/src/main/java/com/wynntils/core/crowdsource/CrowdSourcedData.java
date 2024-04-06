/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.crowdsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataGameVersion;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A holder class for crowd sourced data.
 * This class is only meant to be modified by the {@link CrowdSourcedDataManager}.
 * If you need to access crowd sourced data, use the {@link CrowdSourcedDataManager}.
 */
public class CrowdSourcedData {
    private final Map<CrowdSourcedDataGameVersion, Map<CrowdSourcedDataType, Set<Object>>> data = new TreeMap<>();

    CrowdSourcedData() {}

    CrowdSourcedData(Map<CrowdSourcedDataGameVersion, Map<CrowdSourcedDataType, Set<Object>>> deserializedData) {
        data.putAll(deserializedData);
    }

    <T> void putData(
            CrowdSourcedDataGameVersion version, CrowdSourcedDataType crowdSourcedDataType, T crowdSourceData) {
        if (crowdSourcedDataType.getDataClass() != crowdSourceData.getClass()) {
            throw new IllegalArgumentException("The provided data does not match the crows sourced data type.");
        }

        data.computeIfAbsent(version, k -> new TreeMap<>())
                .computeIfAbsent(crowdSourcedDataType, k -> new TreeSet<>())
                .add(crowdSourceData);
    }

    <T> Set<T> getData(CrowdSourcedDataGameVersion version, CrowdSourcedDataType crowdSourcedDataType, T dataClass) {
        if (crowdSourcedDataType.getDataClass() != dataClass) {
            throw new IllegalArgumentException("The provided data class does not match the crowd sourced data type.");
        }

        return (Set<T>) data.getOrDefault(version, Map.of()).getOrDefault(crowdSourcedDataType, Set.of()).stream()
                .map(crowdSourcedDataType.getDataClass()::cast)
                .collect(Collectors.toSet());
    }

    public static class CrowdSourceDataSerializer implements JsonDeserializer<CrowdSourcedData> {
        @Override
        public CrowdSourcedData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // We could not find any data, return an empty telemetry data
            if (!jsonObject.has("data")) return new CrowdSourcedData();

            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            Map<CrowdSourcedDataGameVersion, Map<CrowdSourcedDataType, Set<Object>>> deserializedData = new TreeMap<>();

            for (Map.Entry<String, JsonElement> gameVersionEntry : dataObject.entrySet()) {
                CrowdSourcedDataGameVersion gameVersion = context.deserialize(
                        new JsonPrimitive(gameVersionEntry.getKey()), CrowdSourcedDataGameVersion.class);
                JsonObject gameVersionObject = gameVersionEntry.getValue().getAsJsonObject();

                Map<CrowdSourcedDataType, Set<Object>> deserializedGameVersionData = new TreeMap<>();

                for (Map.Entry<String, JsonElement> crowdSourceDataTypeEntry : gameVersionObject.entrySet()) {
                    try {
                        CrowdSourcedDataType crowdSourcedDataType = context.deserialize(
                                new JsonPrimitive(crowdSourceDataTypeEntry.getKey()), CrowdSourcedDataType.class);
                        JsonArray crowdSourcedTypeArray =
                                crowdSourceDataTypeEntry.getValue().getAsJsonArray();

                        Set<Object> deserializedCrowdSourcedTypeData = new TreeSet<>();
                        crowdSourcedTypeArray.forEach(entry -> deserializedCrowdSourcedTypeData.add(
                                context.deserialize(entry, crowdSourcedDataType.getDataClass())));

                        deserializedGameVersionData.put(crowdSourcedDataType, deserializedCrowdSourcedTypeData);
                    } catch (Exception exception) { // Catch all exceptions, null fields cause any exceptions
                        // We could not deserialize the crowd sourced data type, skip it
                        WynntilsMod.warn(
                                "Could not deserialize crowd sourced data type: " + crowdSourceDataTypeEntry.getKey()
                                        + " for game version: " + gameVersionEntry.getKey());
                    }
                }

                deserializedData.put(gameVersion, deserializedGameVersionData);
            }

            return new CrowdSourcedData(deserializedData);
        }
    }
}
