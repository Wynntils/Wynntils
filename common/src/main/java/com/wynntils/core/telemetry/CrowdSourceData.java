/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.telemetry.type.CrowdSourceDataGameVersion;
import com.wynntils.core.telemetry.type.CrowdSourceDataType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A holder class for crowd sourced data.
 * This class is only meant to be modified by the {@link CrowdSourceDataManager}.
 * If you need to access crowd source data, use the {@link CrowdSourceDataManager}.
 */
public class CrowdSourceData {
    private final Map<CrowdSourceDataGameVersion, Map<CrowdSourceDataType, Set<Object>>> data = new TreeMap<>();

    CrowdSourceData() {}

    CrowdSourceData(Map<CrowdSourceDataGameVersion, Map<CrowdSourceDataType, Set<Object>>> deserializedData) {
        data.putAll(deserializedData);
    }

    <T> void putData(CrowdSourceDataGameVersion version, CrowdSourceDataType crowdSourceDataType, T crowdSourceData) {
        if (crowdSourceDataType.getDataClass() != crowdSourceData.getClass()) {
            throw new IllegalArgumentException("The provided data does not match the crows source data type.");
        }

        data.computeIfAbsent(version, k -> new TreeMap<>())
                .computeIfAbsent(crowdSourceDataType, k -> new TreeSet<>())
                .add(crowdSourceData);
    }

    <T> Set<T> getData(CrowdSourceDataGameVersion version, CrowdSourceDataType crowdSourceDataType, T dataClass) {
        if (crowdSourceDataType.getDataClass() != dataClass.getClass()) {
            throw new IllegalArgumentException("The provided data class does not match the crowd source data type.");
        }

        return (Set<T>) data.getOrDefault(version, Map.of()).getOrDefault(crowdSourceDataType, Set.of()).stream()
                .map(crowdSourceDataType.getDataClass()::cast)
                .collect(Collectors.toSet());
    }

    public static class CrowdSourceDataSerializer implements JsonDeserializer<CrowdSourceData> {
        @Override
        public CrowdSourceData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // We could not find any data, return an empty telemetry data
            if (!jsonObject.has("data")) return new CrowdSourceData();

            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            Map<CrowdSourceDataGameVersion, Map<CrowdSourceDataType, Set<Object>>> deserializedData = new TreeMap<>();

            for (Map.Entry<String, JsonElement> gameVersionEntry : dataObject.entrySet()) {
                CrowdSourceDataGameVersion gameVersion = context.deserialize(
                        new JsonPrimitive(gameVersionEntry.getKey()), CrowdSourceDataGameVersion.class);
                JsonObject gameVersionObject = gameVersionEntry.getValue().getAsJsonObject();

                Map<CrowdSourceDataType, Set<Object>> deserializedGameVersionData = new TreeMap<>();

                for (Map.Entry<String, JsonElement> crowdSourceDataTypeEntry : gameVersionObject.entrySet()) {
                    CrowdSourceDataType crowdSourceDataType = context.deserialize(
                            new JsonPrimitive(crowdSourceDataTypeEntry.getKey()), CrowdSourceDataType.class);
                    JsonArray crowdSourceTypeArray =
                            crowdSourceDataTypeEntry.getValue().getAsJsonArray();

                    Set<Object> deserializedCrowdSourceTypeData = new TreeSet<>();
                    crowdSourceTypeArray.forEach(entry -> deserializedCrowdSourceTypeData.add(
                            context.deserialize(entry, crowdSourceDataType.getDataClass())));

                    deserializedGameVersionData.put(crowdSourceDataType, deserializedCrowdSourceTypeData);
                }

                deserializedData.put(gameVersion, deserializedGameVersionData);
            }

            return new CrowdSourceData(deserializedData);
        }
    }
}
