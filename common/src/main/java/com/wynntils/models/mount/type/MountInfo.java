/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.type.CappedValue;
import java.util.Map;

public record MountInfo(
        int potential,
        MountColorInfo primaryColorInfo,
        MountColorInfo secondaryColorInfo,
        CappedValue currentEnergy,
        Map<MountStat, CappedValue> stats,
        boolean estimatedMaxStats,
        Map<MountStat, Integer> maxStats) {}
