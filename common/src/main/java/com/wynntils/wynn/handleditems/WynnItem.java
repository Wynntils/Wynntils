/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems;

import com.wynntils.handlers.item.ItemAnnotation;

public class WynnItem implements ItemAnnotation {
    private WynnItemCache cache = new WynnItemCache();

    public WynnItemCache getCache() {
        return cache;
    }
}
