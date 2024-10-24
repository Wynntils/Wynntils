/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public class LeaderboardSeasonItem extends GuiItem implements CountedItemProperty {
    private final int season;
    private final boolean currentSeason;

    public LeaderboardSeasonItem(int season, boolean currentSeason) {
        this.season = season;
        this.currentSeason = currentSeason;
    }

    public int getSeason() {
        return season;
    }

    public boolean isCurrentSeason() {
        return currentSeason;
    }

    @Override
    public int getCount() {
        return season;
    }

    @Override
    public CustomColor getCountColor() {
        return currentSeason ? CommonColors.YELLOW : CommonColors.WHITE;
    }
}
