/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.ChatFormatting;

public class ArchetypeAbilitiesItem extends GuiItem implements CountedItemProperty {
    private final CappedValue count;
    private final ChatFormatting color;

    public ArchetypeAbilitiesItem(CappedValue count, char colorCode) {
        this.count = count;
        this.color = ChatFormatting.getByCode(colorCode);
    }

    @Override
    public int getCount() {
        return count.current();
    }

    @Override
    public boolean hasCount() {
        return count.current() != 0;
    }

    public int getMax() {
        return count.max();
    }

    public ChatFormatting getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "ArchetypeItem{" + "count=" + count + ", color=" + color.getName() + '}';
    }
}
