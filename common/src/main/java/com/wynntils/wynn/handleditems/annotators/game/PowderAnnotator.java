/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.handleditems.items.game.PowderItem;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.PowderProfile;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class PowderAnnotator implements ItemAnnotator {
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("§[2ebcf8].? ?(Earth|Thunder|Water|Fire|Air) Powder ([IV]{1,3})");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = POWDER_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        Powder element = Powder.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        int tier = MathUtils.integerFromRoman(matcher.group(2));

        PowderProfile powderProfile = PowderProfile.getPowderProfile(element, tier);

        return new PowderItem(powderProfile);
    }
}
