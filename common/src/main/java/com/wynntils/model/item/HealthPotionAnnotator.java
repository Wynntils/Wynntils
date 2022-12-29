/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.model.item.game.HealthPotionItem;
import com.wynntils.utils.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class HealthPotionAnnotator implements ItemAnnotator {
    private static final Pattern HEALTH_POTION_PATTERN =
            Pattern.compile("^§c\\[\\+(\\d+) ❤\\] §dPotions of Healing §4\\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        String name = ComponentUtils.getCoded(itemStack.getHoverName());
        Matcher matcher = HEALTH_POTION_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        int hearts = Integer.parseInt(matcher.group(1));
        int uses = Integer.parseInt(matcher.group(2));
        int maxUses = Integer.parseInt(matcher.group(3));

        return new HealthPotionItem(hearts, new CappedValue(uses, maxUses));
    }
}
