/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.custommodel;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CustomModelService extends Service {
    private Map<String, Float> customModelData = new ConcurrentHashMap<>();

    public CustomModelService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MODEL_DATA).handleJsonObject(this::handleModelData);
    }

    Optional<Float> getCustomModelData(String key) {
        return Optional.ofNullable(customModelData.get(key));
    }

    private void handleModelData(JsonObject jsonObject) {
        Map<String, Float> newModelData = new ConcurrentHashMap<>();

        jsonObject.asMap().forEach((key, element) -> {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                newModelData.put(key, element.getAsJsonPrimitive().getAsFloat());
            } else {
                WynntilsMod.error("Invalid custom model data value for key: " + key);
            }
        });

        customModelData = newModelData;
    }
}
