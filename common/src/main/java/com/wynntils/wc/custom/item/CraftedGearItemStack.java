/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import com.wynntils.wc.custom.item.render.HotbarHighlightedItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftedGearItemStack extends WynnItemStack implements HighlightedItem, HotbarHighlightedItem {

    public CraftedGearItemStack(ItemStack stack) {
        super(stack);
    }

    @Override
    public CustomColor getHighlightColor(Screen screen, Slot slot) {
        return ItemTier.CRAFTED.getHighlightColor();
    }

    @Override
    public CustomColor getHotbarColor() {
        return ItemTier.CRAFTED.getHighlightColor();
    }
}
