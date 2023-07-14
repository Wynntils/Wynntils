/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import net.minecraft.ChatFormatting;

public class ArchetypeItem extends GuiItem implements CountedItemProperty {
    private final int count;
    private final ChatFormatting color;

    public ArchetypeItem(int count, char colorCode) {
        this.count = count;
        this.color = ChatFormatting.getByCode(colorCode);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean hasCount() {
        return count != 0;
    }

    public ChatFormatting getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "ArchetypeItem{" + "count=" + count + ", color=" + color.getName() + '}';
    }
}
