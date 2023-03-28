/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import com.wynntils.utils.EnumUtils;
import java.lang.reflect.Type;
import java.util.Set;

public class EnumNamingUpfixer implements ConfigUpfixer {
    @Override
    public boolean apply(JsonObject configObject, Set<ConfigHolder> configHolders) {
        for (ConfigHolder configHolder : configHolders) {
            String name = configHolder.getJsonName();

            Type type = configHolder.getType();
            if (type instanceof Class clazz && clazz.isEnum()) {
                if (!configObject.has(name)) continue;

                JsonPrimitive configValue = configObject.getAsJsonPrimitive(name);
                if (!configValue.isString()) continue;

                String originalValue = configValue.getAsString();
                Enum<?> enumValue = Enum.valueOf(clazz, originalValue);
                String newValue = EnumUtils.toJsonFormat(enumValue);

                configObject.addProperty(name, newValue);
            }
        }

        return true;
    }
}
