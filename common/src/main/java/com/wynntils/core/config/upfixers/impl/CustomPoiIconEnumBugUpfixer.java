/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomPoiIconEnumBugUpfixer implements ConfigUpfixer {
    private static final String POI_LIST_KEY = "mapFeature.customPois";
    private static final Pattern POI_NAME_CHEST_PATTERN = Pattern.compile("Loot Chest T?(\\d)");

    @Override
    public boolean apply(JsonObject configObject, Set<ConfigHolder<?>> configHolders) {
        for (ConfigHolder<?> configHolder : configHolders) {
            if (!configHolder.getJsonName().equals(POI_LIST_KEY)) continue;

            JsonElement obj = configObject.get(POI_LIST_KEY);
            if (obj == null) return true;

            JsonArray array = obj.getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonObject poi = array.get(i).getAsJsonObject();
                if (poi.has("icon")) {
                    String icon = poi.get("icon").getAsString();

                    if (!icon.equals("bubbleBar")) continue;

                    String name = poi.get("name").getAsString();
                    Matcher matcher = POI_NAME_CHEST_PATTERN.matcher(name);

                    if (!matcher.matches()) {
                        // We can't leave the texture as bubble bar...
                        poi.addProperty("icon", "chestT1");
                        continue;
                    }

                    String tier = matcher.group(1);

                    poi.addProperty("icon", "chestT" + tier);
                }
            }
        }

        return true;
    }
}
