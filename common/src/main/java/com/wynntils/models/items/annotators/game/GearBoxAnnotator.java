/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.RangedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GearBoxAnnotator implements GameItemAnnotator {
    private static final Pattern GEAR_BOX_PATTERN = Pattern.compile("^§[5abcdef]Unidentified (.*)$");
    private static final Pattern LEVEL_RANGE_PATTERN = Pattern.compile("^§a- §7Lv\\. Range: §f(\\d+)-(\\d+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;
        Matcher matcher = name.getMatcher(GEAR_BOX_PATTERN);
        if (!matcher.matches()) return null;

        GearType gearType = GearType.fromString(matcher.group(1));
        if (gearType == null) return null;

        GearTier gearTier = GearTier.fromStyledText(name);
        RangedValue levelRange = getLevelRange(itemStack);

        if (gearTier == null || levelRange == null) return null;

        return new GearBoxItem(gearType, gearTier, levelRange);
    }

    private static RangedValue getLevelRange(ItemStack itemStack) {
        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 6, LEVEL_RANGE_PATTERN);
        if (!matcher.matches()) return null;
        int low = Integer.parseInt(matcher.group(1));
        int high = Integer.parseInt(matcher.group(2));
        // Wynncraft "lies" to us, it says like "range 8-12" but in reality this means "9-12".
        // The lowest level is presented as "0-4" so this should be fine
        return RangedValue.of(low + 1, high);
    }
}
