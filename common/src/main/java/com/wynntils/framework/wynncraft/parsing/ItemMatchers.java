/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.wynncraft.parsing;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Tests if an item belongs */
public class ItemMatchers {
    public static boolean isSoulPoint(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() == Items.NETHER_STAR || stack.getItem() == Items.SNOW)
                && stack.getDisplayName().getString().contains("Soul Point");
    }

    public static boolean isPotion(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() == Items.POTION
                && (!stack.getDisplayName()
                                .getString()
                                .contains(ChatFormatting.LIGHT_PURPLE + "Potions of Healing")
                        && !stack.getDisplayName()
                                .getString()
                                .contains(ChatFormatting.RED + "Potion of Healing"));
    }
}
