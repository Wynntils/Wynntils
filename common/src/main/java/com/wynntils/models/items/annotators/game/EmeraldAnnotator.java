/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.models.items.items.game.GameItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class EmeraldAnnotator extends GameItemAnnotator {
    private static final Pattern EMERALD_PATTERN = Pattern.compile("^§a(Liquid )?Emerald( Block)?$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
        EmeraldUnits unit = EmeraldUnits.fromItemType(itemStack.getItem());
        if (unit == null) return null;

        // Verify that name is correct
        Matcher matcher = name.getMatcher(EMERALD_PATTERN);
        if (!matcher.matches()) return null;

        return new EmeraldItem(emeraldPrice, itemStack::getCount, unit);
    }
}
