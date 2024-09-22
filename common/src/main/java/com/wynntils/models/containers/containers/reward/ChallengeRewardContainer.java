/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import java.util.regex.Pattern;

public class ChallengeRewardContainer extends RewardContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Challenge Rewards");

    public ChallengeRewardContainer() {
        super(TITLE_PATTERN);
    }
}
