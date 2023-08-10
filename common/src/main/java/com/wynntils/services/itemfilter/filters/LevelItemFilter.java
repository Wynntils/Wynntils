/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.services.itemfilter.type.ItemFilter;

public class LevelItemFilter implements ItemFilter {
    private final int minLevel;
    private final int maxLevel;

    public LevelItemFilter(int minLevel, int maxLevel) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    public boolean matches(WynnItem wynnItem) {
        return wynnItem instanceof LeveledItemProperty leveledItem
                && leveledItem.getLevel() >= minLevel
                && maxLevel >= leveledItem.getLevel();
    }
}
