/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.gearinfo.type.GearTier;

public record TomeInfo(String displayName, GearTier gearTier, String variant, TomeType type, String tomeTier) {}
