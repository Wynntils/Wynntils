/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.AmplifierItem;
import com.wynntils.utils.MathUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class AmplifierAnnotator implements ItemAnnotator {
    private static final Pattern AMPLIFIER_PATTERN = Pattern.compile("^§bCorkian Amplifier (I{1,3})$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher ampMatcher = name.getMatcher(AMPLIFIER_PATTERN);
        if (!ampMatcher.matches()) return null;

        int tier = MathUtils.integerFromRoman(ampMatcher.group(1));

        return new AmplifierItem(tier);
    }
}
