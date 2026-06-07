/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemdisplaymodel;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ItemDisplayModelService extends Service {
    private Map<Float, String> modelToGroup = new ConcurrentHashMap<>();

    public ItemDisplayModelService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_ITEM_DISPLAY_MODEL_DATA).handleJsonObject(this::handleModelData);
    }

    public Optional<String> getGroup(float modelData) {
        return Optional.ofNullable(modelToGroup.get(modelData));
    }

    private void handleModelData(JsonObject jsonObject) {
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
