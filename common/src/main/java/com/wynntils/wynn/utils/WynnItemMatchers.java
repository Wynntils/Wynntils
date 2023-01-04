/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.CappedValue;
import java.util.List;
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
public final class WynnItemMatchers {
    private static final Pattern SERVER_ITEM_PATTERN = Pattern.compile("§[baec]§lWorld (\\d+)(§3 \\(Recommended\\))?");
    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("(.+)\\[([0-9]+)/([0-9]+)]");
    private static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("§[2ebcf8].? ?(Earth|Thunder|Water|Fire|Air) Powder ([IV]{1,3})");
    private static final Pattern EMERALD_POUCH_TIER_PATTERN = Pattern.compile("Emerald Pouch \\[Tier ([IVX]{1,4})\\]");
    private static final Pattern INGREDIENT_OR_MATERIAL_PATTERN = Pattern.compile("(.*) \\[✫✫✫\\]");

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

    private static boolean isConsumable(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        // consumables are either a potion or a diamond axe for crafteds
        // to ensure an axe item is really a consumable, make sure it has the right name color
        if (itemStack.getItem() != Items.POTION
                && !(itemStack.getItem() == Items.DIAMOND_AXE
                        && itemStack.getHoverName().getString().startsWith(ChatFormatting.DARK_AQUA.toString())))
            return false;

        return consumableNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isGearBox(ItemStack itemStack) {
        return (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6);
    }

    public static boolean isEmeraldPouch(ItemStack itemStack) {
        // Checks for normal emerald pouch (diamond axe) and emerald pouch pickup texture (gold shovel)
        return (itemStack.getItem() == Items.DIAMOND_AXE || itemStack.getItem() == Items.GOLDEN_SHOVEL)
                && itemStack.getHoverName().getString().startsWith("§aEmerald Pouch§2 [Tier");
    }

    /**
     * Returns true if the passed item has an attack speed
     */
    public static boolean isWeapon(ItemStack itemStack) {
        String lore = ItemUtils.getStringLore(itemStack);
        return lore.contains("Attack Speed") && lore.contains("§7");
    }

    /**
     * Returns true if the passed item is a Wynncraft item (armor, weapon, accessory)
     */
    private static boolean isGear(ItemStack itemStack) {
        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (rarityLineMatcher(line).find()) return true;
        }
        return false;
    }

    public static boolean isMythic(ItemStack itemStack) {
        // only gear, identified or not, could be a mythic
        if (!(isGearBox(itemStack) || isGear(itemStack) || isMythicBox(itemStack))) return false;

        return itemStack.getHoverName().getString().contains(ChatFormatting.DARK_PURPLE.toString());
    }

    public static boolean isMythicBox(ItemStack itemStack) {
        return itemStack.is(Items.STONE_SHOVEL) && itemStack.getDamageValue() == 6;
    }

    public static Matcher serverItemMatcher(Component text) {
        return SERVER_ITEM_PATTERN.matcher(text.getString());
    }

    public static Matcher rarityLineMatcher(Component text) {
        return ITEM_RARITY_PATTERN.matcher(text.getString());
    }

    private static Matcher durabilityLineMatcher(Component text) {
        return DURABILITY_PATTERN.matcher(text.getString());
    }

    public static Matcher powderNameMatcher(Component text) {
        return POWDER_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher emeraldPouchTierMatcher(Component text) {
        return EMERALD_POUCH_TIER_PATTERN.matcher(WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(text)));
    }

    private static Matcher consumableNameMatcher(Component text) {
        return CONSUMABLE_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher ingredientOrMaterialMatcher(Component text) {
        return INGREDIENT_OR_MATERIAL_PATTERN.matcher(
                WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(text)));
    }

    public static CappedValue getDurability(ItemStack itemStack) {
        List<Component> lore = itemStack.getTooltipLines(null, TooltipFlag.NORMAL);
        for (Component line : lore) {
            Matcher durabilityMatcher = durabilityLineMatcher(line);
            if (!durabilityMatcher.find()) continue;

            var currentDurability = Integer.parseInt(durabilityMatcher.group(1));
            var maxDurability = Integer.parseInt(durabilityMatcher.group(2));
            return new CappedValue(currentDurability, maxDurability);
        }

        return CappedValue.EMPTY;
    }
}
