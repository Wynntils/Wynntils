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

/** Tests if an item is a certain wynncraft item */
public class WynnItemMatchers {
    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("(.+) \\[([0-9]+)/([0-9]+)]");
    private static final Pattern COSMETIC_PATTERN =
            Pattern.compile("(Common|Rare|Epic|Godly|\\|\\|\\| Black Market \\|\\|\\|) Reward");
    private static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("§[2ebcf8].? ?(Earth|Thunder|Water|Fire|Air|Blank) Powder ([IV]{1,3})");
    private static final Pattern TELEPORT_SCROLL_PATTERN = Pattern.compile("(.*) Teleport Scroll");
    private static final Pattern TELEPORT_LOCATION_PATTERN = Pattern.compile("- Teleports to: (.*)");

    public static boolean isSoulPoint(ItemStack itemStack) {
        return !itemStack.isEmpty()
                && (itemStack.getItem() == Items.NETHER_STAR || itemStack.getItem() == Items.SNOW)
                && itemStack.getDisplayName().getString().contains("Soul Point");
    }

    public static boolean isHealingPotion(ItemStack itemStack) {
        if (!isConsumable(itemStack)) return false;
        if (itemStack.getHoverName().getString().contains(ChatFormatting.LIGHT_PURPLE + "Potions of Healing")
                || itemStack.getHoverName().getString().contains(ChatFormatting.RED + "Potion of Healing")) return true;

        boolean isCraftedPotion = false;
        boolean hasHealEffect = false;
        ListTag lore = ItemUtils.getLoreTagElseEmpty(itemStack);
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

    public static boolean isConsumable(ItemStack itemStack) {
        if (itemStack.isEmpty() || (itemStack.getItem() != Items.POTION && itemStack.getItem() != Items.DIAMOND_AXE))
            return false;

        String name = itemStack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ChatFormatting.stripFormatting(name));
        return CONSUMABLE_PATTERN.matcher(strippedName).matches();
    }

    public static boolean isUnidentified(ItemStack itemStack) {
        return (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6);
    }

    public static boolean isEmeraldPouch(ItemStack itemStack) {
        return itemStack.getHoverName().getString().startsWith("§aEmerald Pouch§2 [Tier");
    }

    public static boolean isHorse(ItemStack itemStack) {
        return itemStack.getItem() == Items.SADDLE
                && itemStack.getHoverName().getString().contains("Horse");
    }

    /**
     * Returns true if the passed item is a Wynncraft item (armor, weapon, accessory)
     */
    public static boolean isGear(ItemStack itemStack) {
        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (rarityLineMatcher(line).find()) return true;
        }
        return false;
    }

    /**
     * Determines if a given ItemStack is an instance of a gear item in the API
     */
    public static boolean isKnownGear(ItemStack itemStack) {
        String name = itemStack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ChatFormatting.stripFormatting(name));
        if (WebManager.getItemsMap() == null || !WebManager.getItemsMap().containsKey(strippedName)) return false;
        ItemProfile profile = WebManager.getItemsMap().get(strippedName);
        return (profile != null
                && name.startsWith(profile.getTier().getChatFormatting().toString()));
    }

    public static boolean isCraftedGear(ItemStack itemStack) {
        String name = itemStack.getHoverName().getString();
        // crafted gear will have a dark aqua name and a % marker for the status of the item
        return (name.startsWith(ChatFormatting.DARK_AQUA.toString()) && name.contains("%"));
    }

    public static boolean isMythic(ItemStack itemStack) {
        // only gear, identified or not, could be a mythic
        if (!(isUnidentified(itemStack) || isGear(itemStack))) return false;

        return itemStack.getHoverName().getString().contains(ChatFormatting.DARK_PURPLE.toString());
    }

    /**
     * Returns true if the passed item has a durability value (crafted items, tools)
     */
    public static boolean isDurabilityItem(ItemStack itemStack) {
        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (durabilityLineMatcher(line).find()) return true;
        }
        return false;
    }

    /**
     * Returns true if the passed item is within the Wynncraft tier system (mythic, legendary, etc.)
     */
    public static boolean isTieredItem(ItemStack itemStack) {
        return isGear(itemStack) || isCraftedGear(itemStack) || isUnidentified(itemStack);
    }

    public static boolean isCosmetic(ItemStack itemStack) {
        for (Component c : ItemUtils.getTooltipLines(itemStack)) {
            if (COSMETIC_PATTERN.matcher(c.getString()).matches()) return true;
        }
        return false;
    }

    public static boolean isPowder(ItemStack itemStack) {
        return powderNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isTeleportScroll(ItemStack itemStack) {
        return teleportScrollNameMatcher(itemStack.getHoverName()).matches();
    }

    public static Matcher rarityLineMatcher(Component text) {
        return ITEM_RARITY_PATTERN.matcher(text.getString());
    }

    public static Matcher durabilityLineMatcher(Component text) {
        return DURABILITY_PATTERN.matcher(text.getString());
    }

    public static Matcher powderNameMatcher(Component text) {
        return POWDER_PATTERN.matcher(text.getString());
    }

    public static Matcher teleportScrollNameMatcher(Component text) {
        return TELEPORT_SCROLL_PATTERN.matcher(text.getString());
    }

    public static Matcher teleportScrollLocationMatcher(Component text) {
        return TELEPORT_LOCATION_PATTERN.matcher(text.getString());
    }
}
