/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.handlers.item.ItemAnnotation;

public class WynnItem implements ItemAnnotation {
    private final WynnItemCache cache = new WynnItemCache();

    public WynnItemCache getCache() {
        return cache;
    }

    @Override
    public String toString() {
        return "WynnItem{}";
    }
}
