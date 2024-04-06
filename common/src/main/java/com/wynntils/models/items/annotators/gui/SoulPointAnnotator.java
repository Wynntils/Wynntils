/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.SoulPointItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SoulPointAnnotator implements ItemAnnotator {
    private static final Pattern SOUL_POINTS_PATTERN = Pattern.compile("^§l(\\d+)§b Soul Points$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.NETHER_STAR) return null;
        Matcher matcher = name.getMatcher(SOUL_POINTS_PATTERN);
        if (!matcher.matches()) return null;

        int count = Integer.parseInt(matcher.group(1));
        return new SoulPointItem(count);
    }
}
