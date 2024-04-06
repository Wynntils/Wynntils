/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class JsonTypeWrapper implements ParameterizedType {
    private final ParameterizedType type;

    public static Type wrap(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return new JsonTypeWrapper(parameterizedType);
        }
        return type;
    }

    private JsonTypeWrapper(ParameterizedType type) {
        this.type = type;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return Arrays.stream(type.getActualTypeArguments())
                .map(JsonTypeWrapper::wrap)
                .toArray(Type[]::new);
    }

    @Override
    public Type getRawType() {
        Type rawType = type.getRawType();

        // Replace abstract collection types with suitable implementations
        if (rawType.equals(List.class)) return ArrayList.class;
        if (rawType.equals(Set.class)) return TreeSet.class;
        if (rawType.equals(Map.class)) return TreeMap.class;

        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return type.getOwnerType();
    }
}
