/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.handleditems.items.game.EmeraldPouchItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EmeraldPouchAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        // Checks for normal emerald pouch (diamond axe) and emerald pouch pickup texture (gold shovel)
        if (itemStack.getItem() != Items.DIAMOND_AXE && itemStack.getItem() != Items.GOLDEN_SHOVEL) return null;

        Matcher matcher = WynnItemMatchers.emeraldPouchTierMatcher(itemStack.getHoverName());
        if (!matcher.matches()) return null;

        int tier = MathUtils.integerFromRoman(matcher.group(1));
        return new EmeraldPouchItem(tier);
    }
}
