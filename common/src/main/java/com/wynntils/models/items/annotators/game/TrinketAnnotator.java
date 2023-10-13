/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.models.items.items.game.TrinketItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class TrinketAnnotator extends GameItemAnnotator {
    private static final Pattern TRINKET_PATTERN = Pattern.compile("^§[5abcdef](.*?)(?: \\[(\\d+)/(\\d+)\\])?$");
    private static final Pattern TRINKET_LORE_PATTERN = Pattern.compile("^§7Right-Click to (use|toggle)$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
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
                return new TrinketItem(emeraldPrice, trinketName, gearTier, uses);
            } else {
                return new TrinketItem(emeraldPrice, trinketName, gearTier);
            }
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }
}
