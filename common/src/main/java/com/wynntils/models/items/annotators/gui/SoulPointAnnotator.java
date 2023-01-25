/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.SoulPointItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SoulPointAnnotator implements ItemAnnotator {
    private static boolean isSoulPoint(ItemStack itemStack) {
        return !itemStack.isEmpty()
                && (itemStack.getItem() == Items.NETHER_STAR || itemStack.getItem() == Items.SNOW)
                && itemStack.getDisplayName().getString().contains("Soul Point");
    }

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        if (!isSoulPoint(itemStack)) return null;

        return new SoulPointItem();
    }
}
