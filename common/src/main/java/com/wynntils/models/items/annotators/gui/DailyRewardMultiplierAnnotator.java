/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.DailyRewardItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class DailyRewardMultiplierAnnotator implements ItemAnnotator {
    private static final StyledText DAILY_REWARD_NAME = StyledText.fromString("§6§lDaily Reward");
    private static final Pattern STREAK_PATTERN = Pattern.compile("^§e✦ Streak Multiplier: §f(\\d+)x$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (!name.equals(DAILY_REWARD_NAME)) return null;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 3, STREAK_PATTERN);
        if (!matcher.matches()) return null;

        int count = Integer.parseInt(matcher.group(1));
        return new DailyRewardItem(count);
    }
}
