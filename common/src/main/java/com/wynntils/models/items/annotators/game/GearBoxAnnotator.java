/*
 * Copyright © Wynntils 2022-2026.
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
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GearBoxAnnotator implements GameItemAnnotator {
    private static final Pattern GEAR_BOX_PATTERN =
            Pattern.compile("^\uDAFC\uDC00§f\uE008\uDB00\uDC02§([5bcdef])Unidentified (.*)\uDAFC\uDC00$");
    private static final Pattern LEVEL_RANGE_PATTERN =
            Pattern.compile("^§#(?:[a-f0-9]{8})(\\d+)-(\\d+) §fLevel Range$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;
        Matcher matcher = name.getMatcher(GEAR_BOX_PATTERN);
        if (!matcher.matches()) return null;

        GearType gearType = GearType.fromString(matcher.group(2));
        if (gearType == null) return null;

        ChatFormatting chatFormatting =
                ChatFormatting.getByCode(matcher.group(1).charAt(0));

        GearTier gearTier = GearTier.fromChatFormatting(chatFormatting);
        RangedValue levelRange = getLevelRange(itemStack);

        if (gearTier == null || levelRange == null) return null;

        return new GearBoxItem(gearType, gearTier, levelRange);
    }

    private static RangedValue getLevelRange(ItemStack itemStack) {
        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 2, LEVEL_RANGE_PATTERN);
        if (!matcher.matches()) return null;
        int low = Integer.parseInt(matcher.group(1));
        int high = Integer.parseInt(matcher.group(2));
        return RangedValue.of(low, high);
    }
}
