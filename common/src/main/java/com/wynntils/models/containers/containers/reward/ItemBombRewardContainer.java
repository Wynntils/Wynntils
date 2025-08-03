/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import java.util.regex.Pattern;

public class ItemBombRewardContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Item Bomb Rewards");

    public ItemBombRewardContainer() {
        super(TITLE_PATTERN);
    }
}
