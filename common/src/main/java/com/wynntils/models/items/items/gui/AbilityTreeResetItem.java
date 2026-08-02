/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

public class AbilityTreeResetItem extends GuiItem {
    private final boolean canReset;

    public AbilityTreeResetItem(boolean canReset) {
        this.canReset = canReset;
    }

    public boolean getCanReset() {
        return canReset;
    }

    @Override
    public String toString() {
        return "AbilityTreeResetItem{" + "canReset=" + canReset + '}';
    }
}
