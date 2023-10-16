/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.SimulatorItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public class SimulatorAnnotator implements ItemAnnotator {
    private static final Pattern SIMULATOR_PATTERN = Pattern.compile("^§(.)Corkian Simulator$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(SIMULATOR_PATTERN);
        if (!matcher.matches()) return null;

        char colorChar = matcher.group(1).charAt(0);
        GearTier gearTier = GearTier.fromChatFormatting(ChatFormatting.getByCode(colorChar));

        if (gearTier == null) return null;

        return new SimulatorItem(gearTier);
    }
}
