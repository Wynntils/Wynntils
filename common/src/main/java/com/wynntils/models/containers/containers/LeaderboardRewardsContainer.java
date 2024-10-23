/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import java.util.regex.Pattern;

public class LeaderboardRewardsContainer extends Container implements ScrollableContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Leaderboard Rewards");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    public LeaderboardRewardsContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public Pattern getNextItemPattern() {
        return NEXT_PAGE_PATTERN;
    }

    @Override
    public Pattern getPreviousItemPattern() {
        return PREVIOUS_PAGE_PATTERN;
    }

    @Override
    public int getNextItemSlot() {
        return 23;
    }

    @Override
    public int getPreviousItemSlot() {
        return 5;
    }
}
