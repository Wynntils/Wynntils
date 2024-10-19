/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.properties;

import com.google.common.collect.ImmutableMap;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.utils.EnumUtils;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SystemPropertiesManager extends Manager {
    private static final Map<Class<?>, Function<String, Object>> MAPPERS = ImmutableMap.of(
            Integer.class, Integer::parseInt,
            Boolean.class, Boolean::parseBoolean,
            URI.class, URI::create);

    public SystemPropertiesManager() {
        super(List.of());
    }

    public <T> T loadJvmArg(Property<T> property) {
        String propertyValue = System.getProperty(property.getFullJvmArgumentPath());

        if (propertyValue == null) {
            return null;
        }

        // Try to convert as an enum first
        if (property.getClassType().isEnum()) {
            try {
                T enumValue = (T) EnumUtils.searchEnum((Class<? extends Enum>) property.getClassType(), propertyValue);
                return enumValue;
            } catch (IllegalArgumentException e) {
                WynntilsMod.warn("Could not parse enum from JVM property " + property.getFullJvmArgumentPath()
                        + ". Using null. Value: " + propertyValue);
                return null;
            }
        }

        // Then fall back to using the mappers
        Function<String, Object> mapper = MAPPERS.get(property.getClassType());
        if (mapper == null) {
            WynntilsMod.error(
                    "Couldn't convert a String JVM property into " + property.getClassType() + ". Using null.");
            return null;
        }

        T value = null;
        try {
            value = (T) mapper.apply(propertyValue);
        } catch (Throwable t) {
            WynntilsMod.error(
                    "Couldn't convert a String JVM property into " + property.getClassType() + ". Using null. Value: "
                            + propertyValue,
                    t);
        }

        return value;
    }
}
