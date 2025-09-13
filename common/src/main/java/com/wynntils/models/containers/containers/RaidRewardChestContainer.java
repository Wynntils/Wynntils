/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import java.util.regex.Pattern;

/**
 * This represents the container for end raid rewards.
 * For the container that displays a preview of available rewards see {@link RaidRewardPreviewContainer}.
 */
public class RaidRewardChestContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFEA\uE00E");

    public static final Pattern REROLL_CONFIRM_PATTERN = Pattern.compile("§7Click again to confirm");
    public static final int REROLL_REWARDS_SLOT = 5;

    public RaidRewardChestContainer() {
        super(TITLE_PATTERN);
    }

    public ContainerBounds getAspectBounds() {
        return new ContainerBounds(1, 2, 1, 6);
    }
}
