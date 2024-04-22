/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.reward;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public abstract class RewardContainer extends Container {
    protected RewardContainer(Pattern titlePattern) {
        super(titlePattern);
    }
}
