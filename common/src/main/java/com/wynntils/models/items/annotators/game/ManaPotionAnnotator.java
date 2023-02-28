/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.ManaPotionItem;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.WynnItemMatchers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ManaPotionAnnotator implements ItemAnnotator {
    private static final Pattern MANA_POTION_PATTERN = Pattern.compile("^§bPotion of Mana§3 \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = MANA_POTION_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        int uses = Integer.parseInt(matcher.group(1));
        int maxUses = Integer.parseInt(matcher.group(2));

        Integer level = WynnItemMatchers.getLevelReq(itemStack, 5);
        if (level == null) return null;

        return new ManaPotionItem(level, new CappedValue(uses, maxUses));
    }
}
