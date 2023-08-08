/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CraftedGearAnnotator implements ItemAnnotator {
    private static final Pattern CRAFTED_GEAR_PATTERN = Pattern.compile("^§3(.*)§b \\[\\d{1,3}%\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(CRAFTED_GEAR_PATTERN);
        if (!matcher.matches()) return null;

        return Models.Gear.parseCraftedGearItem(itemStack);
    }
}
