/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import java.util.regex.Pattern;

public class LootrunRewardContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF2\uE00A");
    private static final Pattern CLOSE_CHEST_PATTERN = Pattern.compile("Close Chest");
    private static final Pattern CONFIRM_SACRIFICE_PATTERN = Pattern.compile("Confirm Sacrifice");
    private static final Pattern CONFIRM_REWARDS_PATTERN = Pattern.compile("Confirm Rewards");

    public LootrunRewardContainer() {
        super(TITLE_PATTERN);
    }

    public Pattern getCloseChestPattern() {
        return CLOSE_CHEST_PATTERN;
    }

    public Pattern getConfirmSacrificePattern() {
        return CONFIRM_SACRIFICE_PATTERN;
    }

    public Pattern getConfirmRewardsPattern() {
        return CONFIRM_REWARDS_PATTERN;
    }
}
