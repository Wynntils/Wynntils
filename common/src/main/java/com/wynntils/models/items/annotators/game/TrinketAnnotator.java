/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.TrinketItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class TrinketAnnotator implements ItemAnnotator {
    private static final Pattern TRINKET_PATTERN = Pattern.compile("^§[5abcdef](.*?)(?: \\[(\\d+)/(\\d+)\\])?$");
    private static final Pattern TRINKET_LORE_PATTERN = Pattern.compile("^§7Right-Click to (use|toggle)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(TRINKET_PATTERN);
        if (!matcher.matches()) return null;

        try {
            // Verify by first line of the lore
            Matcher loreMatcher = LoreUtils.matchLoreLine(itemStack, 0, TRINKET_LORE_PATTERN);
            if (!loreMatcher.matches()) return null;

            String trinketName = matcher.group(1);
            GearTier gearTier = GearTier.fromStyledText(name);
            if (matcher.group(2) != null) {
                CappedValue uses =
                        new CappedValue(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
                return new TrinketItem(trinketName, gearTier, uses);
            } else {
                return new TrinketItem(trinketName, gearTier);
            }
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }
}
