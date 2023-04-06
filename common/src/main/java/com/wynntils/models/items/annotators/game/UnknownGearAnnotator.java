/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText2;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class UnknownGearAnnotator implements ItemAnnotator {
    private static final Pattern UNKNOWN_GEAR_PATTERN = Pattern.compile("^§[5abcdef](.*)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText2 name) {
        GearType gearType = GearType.fromItemStack(itemStack);
        if (gearType == null) return null;

        Matcher matcher = name.match(UNKNOWN_GEAR_PATTERN);
        if (!matcher.matches()) return null;

        String gearName = matcher.group(1);
        GearTier gearTier = GearTier.fromFormattedString(name);

        return Models.Gear.parseUnknownGearItem(gearName, gearType, gearTier, itemStack);
    }
}
