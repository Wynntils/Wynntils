/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.ChatFormatting;

public class ArchetypeAbilitiesItem extends GuiItem implements CountedItemProperty {
    private final CappedValue abilitiesCount;
    private final ChatFormatting color;

    public ArchetypeAbilitiesItem(CappedValue cappedValue, char colorCode) {
        this.abilitiesCount = cappedValue;
        this.color = ChatFormatting.getByCode(colorCode);
    }

    @Override
    public int getCount() {
        return abilitiesCount.current();
    }

    @Override
    public boolean hasCount() {
        return abilitiesCount.current() != 0;
    }

    public CappedValue getAbilitiesCount() {
        return abilitiesCount;
    }

    public ChatFormatting getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "ArchetypeItem{" + "count=" + abilitiesCount + ", color=" + color.getName() + '}';
    }
}
