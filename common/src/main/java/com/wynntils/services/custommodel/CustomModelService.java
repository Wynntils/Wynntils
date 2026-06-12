/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.custommodel;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CustomModelService extends Service {
    private Map<String, Float> floatData = new ConcurrentHashMap<>();
    private Map<String, Pair<Float, Float>> rangeData = new ConcurrentHashMap<>();
    private Map<Float, String> modelToGroup = new ConcurrentHashMap<>();

    public CustomModelService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MODEL_DATA).handleJsonObject(this::handleModelData);
        registry.registerDownload(UrlId.DATA_STATIC_ITEM_DISPLAY_MODEL_DATA)
                .handleJsonObject(this::handleItemDisplayModelData);
    }

    public Optional<Float> getFloat(String key) {
        if (key == null) return Optional.empty();

        return Optional.ofNullable(floatData.get(key));
    }

    public Optional<Pair<Float, Float>> getRange(String key) {
        if (key == null) return Optional.empty();

        return Optional.ofNullable(rangeData.get(key));
    }

    public Optional<String> getGroup(float modelData) {
        return Optional.ofNullable(modelToGroup.get(modelData));
    }

    private void handleModelData(JsonObject jsonObject) {
        Map<String, Float> newFloatData = new ConcurrentHashMap<>();
        Map<String, Pair<Float, Float>> newRangeData = new ConcurrentHashMap<>();

        if (jsonObject.has("floats")) {
            jsonObject.getAsJsonObject("floats").asMap().forEach((key, element) -> {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    newFloatData.put(key, element.getAsJsonPrimitive().getAsFloat());
                } else {
                    WynntilsMod.error("Invalid float custom model data for key: " + key);
                }
            });
        }

        if (jsonObject.has("ranges")) {
            jsonObject.getAsJsonObject("ranges").asMap().forEach((key, element) -> {
                if (element.isJsonArray() && element.getAsJsonArray().size() == 2) {
                    float min = element.getAsJsonArray().get(0).getAsFloat();
                    float max = element.getAsJsonArray().get(1).getAsFloat();
                    newRangeData.put(key, Pair.of(min, max));
                } else {
                    WynntilsMod.error("Invalid range custom model data for key: " + key);
                }
            });
        }

        floatData = newFloatData;
        rangeData = newRangeData;
    }

    private void handleItemDisplayModelData(JsonObject jsonObject) {
        Map<Float, String> newModelToGroup = new ConcurrentHashMap<>();

        if (jsonObject.has("models")) {
            JsonObject modelsObject = jsonObject.getAsJsonObject("models");

            modelsObject.asMap().forEach((groupName, element) -> {
                if (!element.isJsonArray()) {
                    WynntilsMod.error("Invalid item display model data: group '" + groupName + "' is not an array");
                    return;
                }

                element.getAsJsonArray().forEach(item -> {
                    if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isNumber()) {
                        float modelData = item.getAsJsonPrimitive().getAsFloat();
                        newModelToGroup.put(modelData, groupName);
                    } else {
                        WynntilsMod.error(
                                "Invalid item display model data: non-float value in group '" + groupName + "'");
                    }
                });
            });
        }

        modelToGroup = newModelToGroup;
    }
}
