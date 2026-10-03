/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.InsulatorItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class InsulatorAnnotator implements GameItemAnnotator {
    private static final Pattern INSULATOR_PATTERN = Pattern.compile("^§(.)Corkian Insulator$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(INSULATOR_PATTERN);
        if (!matcher.matches()) return null;

        GearTier gearTier = GearTier.fromStyledText(name);

        if (gearTier == null) return null;

        return new InsulatorItem(gearTier);
    }
}
