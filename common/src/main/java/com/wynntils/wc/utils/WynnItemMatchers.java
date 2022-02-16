/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Tests if an item is a certain wynncraft item */
public class WynnItemMatchers {
    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("(.+) \\[([0-9]+)/([0-9]+)]");

    public static boolean isSoulPoint(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() == Items.NETHER_STAR || stack.getItem() == Items.SNOW)
                && stack.getDisplayName().getString().contains("Soul Point");
    }

    public static boolean isHealingPotion(ItemStack stack) {
        return isConsumable(stack)
                && (stack.getHoverName()
                                .getString()
                                .contains(ChatFormatting.LIGHT_PURPLE + "Potions of Healing")
                        || stack.getHoverName()
                                .getString()
                                .contains(ChatFormatting.RED + "Potion of Healing"))
                        || isCraftedHealingPotion(stack);
    }

    public static boolean isCraftedHealingPotion(ItemStack stack) {
        if (!isConsumable(stack)) return false;

        boolean isCraftedPotion = false;
        boolean hasHealEffect = false;
        ListTag lore = ItemUtils.getLoreTagElseEmpty(stack);
        for (Tag tag : lore) {
            String unformattedLoreLine = ComponentUtils.getUnformatted(tag.getAsString());

            if (unformattedLoreLine == null) continue;

            if (unformattedLoreLine.equals("Crafted Potion")) {
                isCraftedPotion = true;
            } else if (unformattedLoreLine.startsWith("- Heal:")) {
                hasHealEffect = true;
            }
        }

        return hasHealEffect && isCraftedPotion;
    }

    public static boolean isConsumable(ItemStack stack) {
        if (stack.isEmpty()
                || (stack.getItem() != Items.POTION && stack.getItem() != Items.DIAMOND_AXE))
            return false;

        return CONSUMABLE_PATTERN
                .matcher(ComponentUtils.getUnformatted(stack.getHoverName()))
                .matches();
    }

    public static boolean isUnidentified(ItemStack stack) {
        return (stack.getItem() == Items.STONE_SHOVEL
                && stack.getDamageValue() >= 1
                && stack.getDamageValue() <= 6);
    }
}
