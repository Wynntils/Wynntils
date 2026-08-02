/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;

public class AbilityTreeItem extends GuiItem implements CountedItemProperty {
    private final int count;
    private final int totalPoints;
    private final boolean canReset;

    public AbilityTreeItem(int count, int totalPoints, Boolean canReset) {
        this.count = count;
        this.totalPoints = totalPoints;
        this.canReset = canReset;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public boolean getCanReset() {
        return canReset;
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
        return "AbilityTreeItem{" + "count=" + count + ", totalPoints=" + totalPoints + ", canReset=" + canReset + '}';
    }
}
