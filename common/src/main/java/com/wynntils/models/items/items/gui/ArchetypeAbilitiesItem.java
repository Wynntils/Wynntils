/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.type.CappedValue;

public class ArchetypeAbilitiesItem extends GuiItem implements CountedItemProperty {
    private final CappedValue abilitiesCount;

    public ArchetypeAbilitiesItem(CappedValue cappedValue) {
        this.abilitiesCount = cappedValue;
    }

    @Override
    public int getCount() {
        return abilitiesCount.current();
    }

    @Override
    public boolean hasCount() {
        return abilitiesCount.current() != 0;
    }

    public CappedValue getAbilitiesCount() {
        return abilitiesCount;
    }

    @Override
    public String toString() {
        return "ArchetypeItem{" + "count=" + abilitiesCount + '}';
    }
}
