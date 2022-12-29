/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.game.PowderItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.generator.PowderGenerator;
import com.wynntils.wynn.item.generator.PowderProfile;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.Powder;
import java.util.Locale;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class PowderAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher matcher = WynnItemMatchers.powderNameMatcher(itemStack.getHoverName());

        if (!matcher.matches()) return null;

        Powder element = Powder.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        int tier = MathUtils.integerFromRoman(matcher.group(2));

        PowderProfile powderProfile = PowderGenerator.getPowderProfile(element, tier);

        return new PowderItem(powderProfile);
    }
}
