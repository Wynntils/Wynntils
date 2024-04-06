/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class RenamedPrefixesUpfixer implements Upfixer {
    protected abstract List<Pair<String, String>> getRenamedPrefixes();

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        List<String> configKeys = new ArrayList<>(configObject.keySet());
        for (String configName : configKeys) {
            for (Pair<String, String> prefix : getRenamedPrefixes()) {
                String oldPrefix = prefix.a();
                String newPrefix = prefix.b();

                if (configName.startsWith(oldPrefix)) {
                    String newConfigName = newPrefix + configName.substring(oldPrefix.length());
                    JsonElement value = configObject.get(configName);
                    configObject.add(newConfigName, value);
                    configObject.remove(configName);
                }
            }
        }

        return true;
    }
}
