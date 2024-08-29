/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CorruptedCacheItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public class CorruptedCacheAnnotator implements GameItemAnnotator {
    private static final Pattern CACHE_PATTERN = Pattern.compile("^§(.)Corrupted Cache$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(CACHE_PATTERN);
        if (!matcher.matches()) return null;

        char colorChar = matcher.group(1).charAt(0);
        GearTier gearTier = GearTier.fromChatFormatting(ChatFormatting.getByCode(colorChar));

        if (gearTier == null) return null;

        return new CorruptedCacheItem(gearTier);
    }
}
