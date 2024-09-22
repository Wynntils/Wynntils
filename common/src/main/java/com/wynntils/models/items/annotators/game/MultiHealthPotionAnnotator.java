/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.MultiHealthPotionItem;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class MultiHealthPotionAnnotator implements GameItemAnnotator {
    private static final Pattern MULTI_HEALTH_POTION_PATTERN =
            Pattern.compile("^§c\\[\\+(\\d+) ❤\\] §dPotions of Healing §4\\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(MULTI_HEALTH_POTION_PATTERN);
        if (!matcher.matches()) return null;

        int hearts = Integer.parseInt(matcher.group(1));
        int uses = Integer.parseInt(matcher.group(2));
        int maxUses = Integer.parseInt(matcher.group(3));

        return new MultiHealthPotionItem(hearts, new CappedValue(uses, maxUses));
    }
}
