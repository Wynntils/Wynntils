/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EmeraldPouchAnnotator implements GameItemAnnotator {
    private static final Pattern EMERALD_POUCH_PATTERN = Pattern.compile("^§aEmerald Pouch §2\\[Tier (\\d+)\\]$");
    private static final Pattern EMERALD_POUCH_LORE_PATTERN =
            Pattern.compile("§6§l([\\d\\s,]+)" + EmeraldUnits.EMERALD.getSymbol() + ".*");
    private static final Pattern NUMBER_FORMAT_PATTERN = Pattern.compile("[\\s,]");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        // Checks for normal emerald pouch (diamond axe) and emerald pouch pickup texture (gold shovel)
        if (itemStack.getItem() != Items.DIAMOND_AXE && itemStack.getItem() != Items.GOLDEN_SHOVEL) return null;

        Matcher matcher = name.getMatcher(EMERALD_POUCH_PATTERN);
        if (!matcher.matches()) return null;

        int tier = Integer.parseInt(matcher.group(1));

        Matcher amountMatcher = LoreUtils.matchLoreLine(itemStack, 0, EMERALD_POUCH_LORE_PATTERN);
        // This can be an emerald pouch on the trade market, it has no amount line
        if (!amountMatcher.matches()) return new EmeraldPouchItem(tier, 0);

        int amount = Integer.parseInt(
                NUMBER_FORMAT_PATTERN.matcher(amountMatcher.group(1)).replaceAll(""));

        return new EmeraldPouchItem(tier, amount);
    }
}
