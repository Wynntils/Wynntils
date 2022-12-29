/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.game.AmplifierItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class AmplifierAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher ampMatcher = WynnItemMatchers.amplifierNameMatcher(itemStack.getHoverName());
        if (!ampMatcher.matches()) return null;

        int tier = MathUtils.integerFromRoman(ampMatcher.group(1));

        return new AmplifierItem(tier);
    }
}
