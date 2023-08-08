/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;

public class SoulPointItem extends GuiItem implements CountedItemProperty {
    private final int count;

    public SoulPointItem(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "SoulPointItem{" + "count=" + count + '}';
    }
}
