/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WynnItemCache {
    public static final String HIGHLIGHT_KEY = "highlight";
    public static final String OVERLAY_KEY = "overlay";
    public static final String TOOLTIP_KEY = "tooltip";
    public static final String SEARCHED_KEY = "searched";
    public static final String FAVORITE_KEY = "favorite";

    private final Map<String, Object> cache = new HashMap<>();

    public <T> T get(String key) {
        return (T) cache.get(key);
    }

    public <T> T getOrCalculate(String key, Supplier<T> calculator) {
        if (!cache.containsKey(key)) {
            T value = calculator.get();
            store(key, value);
            return value;
        } else {
            return get(key);
        }
    }

    public <T> void store(String key, T obj) {
        cache.put(key, obj);
    }

    public <T> void clear(String key) {
        cache.remove(key);
    }

    public void clearAll() {
        cache.clear();
    }
}
