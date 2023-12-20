/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.type.RangedValue;

public record CharmRequirements(int level, RangedValue workingLevelRange) {}
