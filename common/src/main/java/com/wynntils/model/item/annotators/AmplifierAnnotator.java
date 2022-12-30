/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.game.AmplifierItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AmplifierAnnotator implements ItemAnnotator {
    private static final Pattern AMPLIFIER_PATTERN = Pattern.compile("§bCorkian Amplifier (I{1,3})");

    public static Matcher amplifierNameMatcher(Component text) {
        return AMPLIFIER_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher ampMatcher = amplifierNameMatcher(itemStack.getHoverName());
        if (!ampMatcher.matches()) return null;

        int tier = MathUtils.integerFromRoman(ampMatcher.group(1));

        return new AmplifierItem(tier);
    }
}
