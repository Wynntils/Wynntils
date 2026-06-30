/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;

public class AbilityTreeItem extends GuiItem implements CountedItemProperty {
    private final int count;
    private final int totalPoints;

    public AbilityTreeItem(int count, int totalPoints) {
        this.count = count;
        this.totalPoints = totalPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean hasCount() {
        return count != 0;
    }

    @Override
    public String toString() {
        return "AbilityTreeItem{" + "count=" + count + ", totalPoints=" + totalPoints + '}';
    }
}
