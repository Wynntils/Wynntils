/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import java.util.regex.Pattern;

public class IngredientBombRewardContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Ingredient Bomb Rewards");

    public IngredientBombRewardContainer() {
        super(TITLE_PATTERN);
    }
}
