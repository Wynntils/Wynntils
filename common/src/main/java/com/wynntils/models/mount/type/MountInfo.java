/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.type.CappedValue;
import java.util.Map;
import java.util.Optional;

public record MountInfo(
        int potential,
        Optional<String> primaryColor,
        Optional<String> secondaryColor,
        CappedValue currentEnergy,
        Map<MountStat, CappedValue> stats,
        Map<MountStat, Integer> maxStats) {}
