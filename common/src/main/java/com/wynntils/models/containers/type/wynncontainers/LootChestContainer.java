/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.core.components.Models;
import com.wynntils.models.containers.type.RewardContainer;
import java.util.regex.Pattern;

public class LootChestContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Models.LootChest.LOOT_CHEST_PATTERN;

    public LootChestContainer() {
        super(TITLE_PATTERN);
    }
}
