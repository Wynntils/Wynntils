/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import java.util.regex.Pattern;

public class EventContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Event Rewards");

    public EventContainer() {
        super(TITLE_PATTERN);
    }
}
