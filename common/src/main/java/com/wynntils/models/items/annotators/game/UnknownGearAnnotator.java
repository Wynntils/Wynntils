/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GameItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class UnknownGearAnnotator extends GameItemAnnotator {
    private static final Pattern UNKNOWN_GEAR_PATTERN = Pattern.compile("^§[5abcdef](.*)$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
        GearType gearType = GearType.fromItemStack(itemStack);
        if (gearType == null) return null;

        Matcher matcher = name.getMatcher(UNKNOWN_GEAR_PATTERN);
        if (!matcher.matches()) return null;

        String gearName = matcher.group(1);
        GearTier gearTier = GearTier.fromStyledText(name);

        return Models.Gear.parseUnknownGearItem(gearName, gearType, gearTier, itemStack, emeraldPrice);
    }
}
