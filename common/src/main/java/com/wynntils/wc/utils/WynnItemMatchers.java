/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

/** Tests if an item is a certain wynncraft item */
public class WynnItemMatchers {
    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("(.+) \\[([0-9]+)/([0-9]+)]");
    private static final Pattern COSMETIC_PATTERN =
            Pattern.compile("(Common|Rare|Epic|Godly|\\|\\|\\| Black Market \\|\\|\\|) Reward");

    private static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic) Item.*");

    public static boolean isSoulPoint(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() == Items.NETHER_STAR || stack.getItem() == Items.SNOW)
                && stack.getDisplayName().getString().contains("Soul Point");
    }

    public static boolean isHealingPotion(ItemStack stack) {
        if (!isConsumable(stack)) return false;
        if (stack.getHoverName().getString().contains(ChatFormatting.LIGHT_PURPLE + "Potions of Healing")
                || stack.getHoverName().getString().contains(ChatFormatting.RED + "Potion of Healing")) return true;

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

        return isCraftedPotion && hasHealEffect;
    }

    public static boolean isConsumable(ItemStack stack) {
        if (stack.isEmpty() || (stack.getItem() != Items.POTION && stack.getItem() != Items.DIAMOND_AXE)) return false;

        String name = stack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ChatFormatting.stripFormatting(name));
        return CONSUMABLE_PATTERN.matcher(strippedName).matches();
    }

    public static boolean isUnidentified(ItemStack stack) {
        return (stack.getItem() == Items.STONE_SHOVEL && stack.getDamageValue() >= 1 && stack.getDamageValue() <= 6);
    }

    public static boolean isEmeraldPouch(ItemStack stack) {
        return stack.getHoverName().getString().startsWith("§aEmerald Pouch§2 [Tier");
    }

    public static boolean isHorse(ItemStack stack) {
        return stack.getItem() == Items.SADDLE
                && stack.getHoverName().getString().contains("Horse");
    }

    /**
     * Returns true if the passed item is a Wynncraft item (armor, weapon, accessory)
     */
    public static boolean isGear(ItemStack stack) {
        for (Component line : ItemUtils.getTooltipLines(stack)) {
            if (isRarityLine(line)) return true;
        }
        return false;
    }

    /**
     * Determines if a given ItemStack is an instance of a gear item in the API
     */
    public static boolean isKnownGear(ItemStack stack) {
        String name = stack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ChatFormatting.stripFormatting(name));
        if (WebManager.getItemsMap() == null || !WebManager.getItemsMap().containsKey(strippedName)) return false;
        ItemProfile profile = WebManager.getItemsMap().get(strippedName);
        return (profile != null
                && name.startsWith(profile.getTier().getChatFormatting().toString()));
    }

    public static boolean isCraftedGear(ItemStack stack) {
        String name = stack.getHoverName().getString();
        // crafted gear will have a dark aqua name and a % marker for the status of the item
        return (name.startsWith(ChatFormatting.DARK_AQUA.toString()) && name.contains("%"));
    }

    public static boolean isCosmetic(ItemStack stack) {
        for (Component c : stack.getTooltipLines(null, TooltipFlag.Default.NORMAL)) {
            if (COSMETIC_PATTERN.matcher(c.getString()).matches()) return true;
        }
        return false;
    }

    public static boolean isMythic(ItemStack stack) {
        // only gear, identified or not, could be a mythic
        if (!(isUnidentified(stack) || isGear(stack))) return false;

        return stack.getHoverName().getString().contains(ChatFormatting.DARK_PURPLE.toString());
    }

    public static boolean isRarityLine(Component line) {
        Matcher rarityMatcher = ITEM_RARITY_PATTERN.matcher(line.getString());
        return rarityMatcher.find();
    }
}
