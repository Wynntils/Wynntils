/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.LeaderboardSeasonItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class LeaderboardSeasonAnnotator implements GuiItemAnnotator {
    // Test in LeaderboardSeasonAnnotator_SEASON_PATTERN
    private static final Pattern SEASON_PATTERN = Pattern.compile("^§d§lSeason (\\d+)$");
    private static final Pattern LORE_PATTERN = Pattern.compile("^§7Current Season$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(SEASON_PATTERN);
        if (!matcher.matches()) return null;

        int season = Integer.parseInt(matcher.group(1));

        Matcher m = LoreUtils.matchLoreLine(itemStack, 0, LORE_PATTERN);
        boolean currentSeason = m.matches();

        return new LeaderboardSeasonItem(season, currentSeason);
    }
}
