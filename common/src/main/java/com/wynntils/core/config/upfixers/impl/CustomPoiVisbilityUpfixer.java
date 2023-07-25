/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import com.wynntils.services.map.pois.CustomPoi;
import java.util.Set;

public class CustomPoiVisbilityUpfixer implements ConfigUpfixer {
    private static final String CUSTOM_POIS_ARRAY = "mapFeature.customPois";
    private static final String CUSTOM_POIS_VISIBILITY = "visibility";
    private static final String CUSTOM_POIS_MINZOOM = "minZoom";

    @Override
    public boolean apply(JsonObject configObject, Set<ConfigHolder> configHolders) {
        JsonArray customPois = configObject.getAsJsonArray(CUSTOM_POIS_ARRAY);

        if (customPois == null) return true;

        for (JsonElement pois : customPois) {
            JsonObject poi = pois.getAsJsonObject();

            if (poi.has(CUSTOM_POIS_MINZOOM)) {
                float oldMinZoom = poi.getAsJsonPrimitive(CUSTOM_POIS_MINZOOM).getAsFloat();

                CustomPoi.Visibility visibility = CustomPoi.Visibility.DEFAULT;

                if (oldMinZoom == Integer.MAX_VALUE) {
                    visibility = CustomPoi.Visibility.HIDDEN;
                } else if (oldMinZoom == Integer.MIN_VALUE) {
                    visibility = CustomPoi.Visibility.ALWAYS;
                }

                poi.addProperty(CUSTOM_POIS_VISIBILITY, visibility.name());
                poi.remove(CUSTOM_POIS_MINZOOM);
                continue;
            }

            if (!poi.has(CUSTOM_POIS_VISIBILITY)
                    || poi.get(CUSTOM_POIS_VISIBILITY).isJsonNull()) {
                poi.addProperty(CUSTOM_POIS_VISIBILITY, CustomPoi.Visibility.DEFAULT.name());
            }
        }

        return true;
    }

    @Override
    public String getUpfixerName() {
        // This must not be run twice, so keep the old name
        return "custom_poi_visibility";
    }
}
