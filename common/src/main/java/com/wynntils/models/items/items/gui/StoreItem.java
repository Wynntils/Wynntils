/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.utils.colors.CustomColor;

public class StoreItem extends GuiItem {
    private final CustomColor highlightColor;

    public StoreItem(CustomColor highlightColor) {
        this.highlightColor = highlightColor;
    }

    public CustomColor getHighlightColor() {
        return highlightColor;
    }

    @Override
    public String toString() {
        return "StoreItem{" + "highlightColor=" + highlightColor + '}';
    }
}
