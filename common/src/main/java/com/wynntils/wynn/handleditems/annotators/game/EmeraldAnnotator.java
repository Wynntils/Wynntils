/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.wynn.handleditems.items.game.EmeraldItem;
import com.wynntils.wynn.objects.EmeraldUnits;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class EmeraldAnnotator implements ItemAnnotator {
    private static final Pattern EMERALD_POUCH_TIER_PATTERN = Pattern.compile("^§a(Liquid )?Emerald( Block)?$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        EmeraldUnits unit = EmeraldUnits.fromItemType(itemStack.getItem());
        if (unit == null) return null;

        // Verify that name is correct
        Matcher matcher = EMERALD_POUCH_TIER_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        return new EmeraldItem(itemStack.getCount(), unit);
    }
}
