/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;

public class AbilityTreeItem extends GuiItem implements CountedItemProperty {
    private final int count;
    private final int totalPoints;
    private final int loanedPoints;
    private final boolean canReset;

    public AbilityTreeItem(int count, int totalPoints, int loanedPoints, Boolean canReset) {
        this.count = count;
        this.totalPoints = totalPoints;
        this.loanedPoints = loanedPoints;
        this.canReset = canReset;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getLoanedPoints() {
        return loanedPoints;
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
        return "AbilityTreeItem{" + "count=" + count + ", totalPoints=" + totalPoints + ", loanedPoints=" + loanedPoints
                + ", canReset=" + canReset + '}';
    }
}
