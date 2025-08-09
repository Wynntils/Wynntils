/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class RaidRewardChestContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFEA\uE00E");

    public static final Pattern REROLL_CONFIRM_PATTERN = Pattern.compile("§7Click again to confirm");
    public static final int REROLL_REWARDS_SLOT = 5;

    public RaidRewardChestContainer() {
        super(TITLE_PATTERN);
    }
}
