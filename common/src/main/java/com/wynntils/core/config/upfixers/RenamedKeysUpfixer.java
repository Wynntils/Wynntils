/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.config.Config;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Set;

public abstract class RenamedKeysUpfixer implements ConfigUpfixer {
    protected abstract List<Pair<String, String>> getRenamedKeys();

    @Override
    public boolean apply(JsonObject configObject, Set<Config<?>> configs) {
        for (Pair<String, String> renamePair : getRenamedKeys()) {
            String oldName = renamePair.a();
            String newName = renamePair.b();
            if (!configObject.has(oldName)) continue;

            JsonElement jsonElement = configObject.get(oldName);
            configObject.remove(oldName);

            configObject.add(newName, jsonElement);
        }

        return true;
    }
}
