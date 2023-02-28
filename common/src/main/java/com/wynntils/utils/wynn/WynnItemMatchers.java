/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

/** Tests if an item is a certain wynncraft item */
public final class WynnItemMatchers {
    // https://regexr.com/798o0
    public static final Pattern LEVEL_MATCHER = Pattern.compile("^§..§r§7 Combat Lv. Min: (\\d+)$");

    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("(.+)\\[([0-9]+)/([0-9]+)]");
    private static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");

    public static boolean isHealingPotion(ItemStack itemStack) {
        if (!isConsumable(itemStack)) return false;
        if (itemStack.getHoverName().getString().contains(ChatFormatting.LIGHT_PURPLE + "Potions of Healing")
                || itemStack.getHoverName().getString().contains(ChatFormatting.RED + "Potion of Healing")) return true;

        boolean isCraftedPotion = false;
        boolean hasHealEffect = false;
        ListTag lore = LoreUtils.getLoreTagElseEmpty(itemStack);
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

    /**
     * Returns true if the passed item has an attack speed
     */
    public static boolean isWeapon(ItemStack itemStack) {
        String lore = LoreUtils.getStringLore(itemStack);
        return lore.contains("Attack Speed") && lore.contains("§7");
    }

    /**
     * Returns true if the passed item is a Wynncraft item (armor, weapon, accessory)
     */
    private static boolean isGear(ItemStack itemStack) {
        for (Component line : LoreUtils.getTooltipLines(itemStack)) {
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

    public static Matcher rarityLineMatcher(Component text) {
        return ITEM_RARITY_PATTERN.matcher(text.getString());
    }

    private static Matcher durabilityLineMatcher(Component text) {
        return DURABILITY_PATTERN.matcher(text.getString());
    }

    private static Matcher consumableNameMatcher(Component text) {
        return CONSUMABLE_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
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

    public static MutableComponent getNonGearDescription(ItemStack itemStack, String gearName) {
        if (gearName.contains("Crafted")) {
            return Component.literal(gearName).withStyle(ChatFormatting.DARK_AQUA);
        }

        // this solves an unidentified item showcase exploit
        // boxes items are STONE_SHOVEL, 1 represents UNIQUE boxes and 6 MYTHIC boxes
        if (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6) {
            return Component.literal("Unidentified Item")
                    .withStyle(
                            GearTier.fromBoxDamage(itemStack.getDamageValue()).getChatFormatting());
        }
        return null;
    }

    public static Integer getLevelReq(ItemStack itemStack, int startLineNum) {
        Matcher levelMatcher = LoreUtils.matchLoreLine(itemStack, startLineNum, LEVEL_MATCHER);
        if (!levelMatcher.matches()) return null;

        return Integer.parseInt(levelMatcher.group(1));
    }
}
