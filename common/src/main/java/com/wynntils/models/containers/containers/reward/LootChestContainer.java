/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import java.util.regex.Pattern;

public class LootChestContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Loot Chest (§.)\\[.+\\]");

    public LootChestContainer() {
        super(TITLE_PATTERN);
    }
}
