/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.utils.colors.CustomColor;

public class CosmeticItem extends GuiItem {
    private final CustomColor highlightColor;

    public CosmeticItem(CustomColor highlightColor) {
        this.highlightColor = highlightColor;
    }

    public CustomColor getHighlightColor() {
        return highlightColor;
    }

    @Override
    public String toString() {
        return "CosmeticItem{" + "highlightColor=" + highlightColor + '}';
    }
}
