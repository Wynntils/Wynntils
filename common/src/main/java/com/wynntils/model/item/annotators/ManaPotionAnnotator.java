/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.model.item.game.ManaPotionItem;
import com.wynntils.utils.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class ManaPotionAnnotator implements ItemAnnotator {
    private static final Pattern MANA_POTION_PATTERN = Pattern.compile("^§bPotion of Mana§3 \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        String name = ComponentUtils.getCoded(itemStack.getHoverName());
        Matcher matcher = MANA_POTION_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        int uses = Integer.parseInt(matcher.group(1));
        int maxUses = Integer.parseInt(matcher.group(2));

        return new ManaPotionItem(new CappedValue(uses, maxUses));
    }
}
