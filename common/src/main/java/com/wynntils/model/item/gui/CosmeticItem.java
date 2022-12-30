/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.gui;

import com.wynntils.mc.objects.CustomColor;

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
