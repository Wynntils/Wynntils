/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.type.StyledText;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

/** Tests if an item is a certain wynncraft item */
public final class WynnItemMatchers {
    private static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");

    public static boolean isGearBox(ItemStack itemStack) {
        return (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6);
    }

    /**
     * Returns true if the passed item has an attack speed
     */
    public static boolean isWeapon(ItemStack itemStack) {
        StyledText lore = LoreUtils.getStringLore(itemStack);
        return lore.str().contains("Attack Speed") && lore.str().contains("§7");
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
}
