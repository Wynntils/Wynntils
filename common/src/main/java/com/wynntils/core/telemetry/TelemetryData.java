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
import com.wynntils.core.telemetry.type.TelemetryGameVersion;
import com.wynntils.core.telemetry.type.TelemetryType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A holder class for telemetry data.
 * This class is only meant to be modified by the {@link TelemetryManager}.
 * If you need to access telemetry data, use the {@link TelemetryManager}.
 */
public class TelemetryData {
    private final Map<TelemetryGameVersion, Map<TelemetryType, Set<Object>>> data = new TreeMap<>();

    TelemetryData() {}

    TelemetryData(Map<TelemetryGameVersion, Map<TelemetryType, Set<Object>>> deserializedData) {
        data.putAll(deserializedData);
    }

    <T> void putData(TelemetryGameVersion version, TelemetryType telemetryType, T telemetryData) {
        if (telemetryType.getDataClass() != telemetryData.getClass()) {
            throw new IllegalArgumentException("The provided data does not match the telemetry type.");
        }

        data.computeIfAbsent(version, k -> new TreeMap<>())
                .computeIfAbsent(telemetryType, k -> new TreeSet<>())
                .add(telemetryData);
    }

    <T> Set<T> getData(TelemetryGameVersion version, TelemetryType telemetryType, T dataClass) {
        if (telemetryType.getDataClass() != dataClass.getClass()) {
            throw new IllegalArgumentException("The provided data class does not match the telemetry type.");
        }

        return (Set<T>) data.getOrDefault(version, Map.of()).getOrDefault(telemetryType, Set.of()).stream()
                .map(telemetryType.getDataClass()::cast)
                .collect(Collectors.toSet());
    }

    public static class TelemetryDataSerializer implements JsonDeserializer<TelemetryData> {
        @Override
        public TelemetryData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // We could not find any data, return an empty telemetry data
            if (!jsonObject.has("data")) return new TelemetryData();

            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            Map<TelemetryGameVersion, Map<TelemetryType, Set<Object>>> deserializedData = new TreeMap<>();

            for (Map.Entry<String, JsonElement> gameVersionEntry : dataObject.entrySet()) {
                TelemetryGameVersion gameVersion =
                        context.deserialize(new JsonPrimitive(gameVersionEntry.getKey()), TelemetryGameVersion.class);
                JsonObject gameVersionObject = gameVersionEntry.getValue().getAsJsonObject();

                Map<TelemetryType, Set<Object>> deserializedGameVersionData = new TreeMap<>();

                for (Map.Entry<String, JsonElement> telemetryTypeEntry : gameVersionObject.entrySet()) {
                    TelemetryType telemetryType =
                            context.deserialize(new JsonPrimitive(telemetryTypeEntry.getKey()), TelemetryType.class);
                    JsonArray telemetryTypeArray = telemetryTypeEntry.getValue().getAsJsonArray();

                    Set<Object> deserializedTelemetryTypeData = new TreeSet<>();
                    telemetryTypeArray.forEach(entry -> deserializedTelemetryTypeData.add(
                            context.deserialize(entry, telemetryType.getDataClass())));

                    deserializedGameVersionData.put(telemetryType, deserializedTelemetryTypeData);
                }

                deserializedData.put(gameVersion, deserializedGameVersionData);
            }

            return new TelemetryData(deserializedData);
        }
    }
}
