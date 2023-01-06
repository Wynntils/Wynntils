/*
 * Copyright © Wynntils 2022, 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.handleditems.items.game.EmeraldPouchItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EmeraldPouchAnnotator implements ItemAnnotator {
    private static final Pattern EMERALD_POUCH_TIER_PATTERN =
            Pattern.compile("^§aEmerald Pouch§2 \\[Tier ([IVX]{1,4})\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        // Checks for normal emerald pouch (diamond axe) and emerald pouch pickup texture (gold shovel)
        if (itemStack.getItem() != Items.DIAMOND_AXE && itemStack.getItem() != Items.GOLDEN_SHOVEL) return null;

        Matcher matcher = EMERALD_POUCH_TIER_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        int tier = MathUtils.integerFromRoman(matcher.group(1));
        return new EmeraldPouchItem(tier);
    }
}
