/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.world.item.ItemStack;

public class ItemUtils {
    public static boolean isEquals(ItemStack firstItem, ItemStack secondItem) {
        if (!firstItem.getItem().equals(secondItem.getItem())) return false;
        if (firstItem.getCount() != secondItem.getCount()) return false;
        if (firstItem.getTag() == null) return secondItem.getTag() == null;
        if (!firstItem.getTag().equals(secondItem.getTag())) return false;
        return true;
    }
}
