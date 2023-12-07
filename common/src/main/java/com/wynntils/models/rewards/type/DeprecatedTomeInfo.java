/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.TomeType;

public record DeprecatedTomeInfo(
        String displayName, GearTier gearTier, String variant, TomeType type, String tomeTier) {}
