/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import java.util.regex.Pattern;

/**
 * This represents the container for previewing available raid rewards.
 * For the container used to display rewards from previous raid see {@link RaidRewardChestContainer}.
 */
public class RaidRewardPreviewContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFEA\uE00D\uDAFF\uDF6F.");

    public RaidRewardPreviewContainer() {
        super(TITLE_PATTERN);
    }

    public ContainerBounds getRewardBounds() {
        return new ContainerBounds(3, 0, 5, 8);
    }
}
