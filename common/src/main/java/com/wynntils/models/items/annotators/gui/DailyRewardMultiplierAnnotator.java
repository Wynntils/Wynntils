/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.LoreUtils;
import com.wynntils.models.items.items.gui.DailyRewardItem;
import net.minecraft.world.item.ItemStack;

public final class DailyRewardMultiplierAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        if (!name.contains("Daily Reward")) return null;

        try {
            // Multiplier line is always on index 3
            String loreLine =
                    ComponentUtils.stripFormatting(LoreUtils.getLore(itemStack).get(3));
            String value = String.valueOf(loreLine.charAt(loreLine.indexOf("Streak Multiplier: ") + 19));
            int count = Integer.parseInt(value);
            return new DailyRewardItem(count);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }
}
