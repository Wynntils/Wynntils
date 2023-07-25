/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapToMainMapRenamedConfigsUpfixer implements ConfigUpfixer {
    @Override
    public boolean apply(JsonObject configObject, Set<ConfigHolder> configHolders) {
        List<String> configKeys = new ArrayList<>(configObject.keySet());
        for (String configName : configKeys) {
            if (configName.startsWith("mapFeature.")) {
                String newConfigName = configName.replace("mapFeature.", "mainMapFeature.");
                JsonElement value = configObject.get(configName);
                configObject.add(newConfigName, value);
                configObject.remove(configName);
            }
        }

        return true;
    }
}
