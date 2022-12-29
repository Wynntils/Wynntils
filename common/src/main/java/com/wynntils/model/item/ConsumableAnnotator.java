/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.game.ConsumableItem;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ConsumableAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        // consumables are either a potion or a diamond axe for crafteds
        // to ensure an axe item is really a consumable, make sure it has the right name color
        if (itemStack.getItem() != Items.POTION
                && !(itemStack.getItem() == Items.DIAMOND_AXE
                        && itemStack.getHoverName().getString().startsWith(ChatFormatting.DARK_AQUA.toString()))) {
            return null;
        }

        Matcher consumableMatcher = WynnItemMatchers.consumableNameMatcher(itemStack.getHoverName());
        if (!consumableMatcher.matches()) return null;

        int charges = Integer.parseInt(consumableMatcher.group(2));
        int maxCharges = Integer.parseInt(consumableMatcher.group(3));
        return new ConsumableItem(new CappedValue(charges, maxCharges));
    }
}
