/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.RewardContainer;
import java.util.regex.Pattern;

public class FlyingChestContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Flying Chest");

    public FlyingChestContainer() {
        super(TITLE_PATTERN);
    }
}
