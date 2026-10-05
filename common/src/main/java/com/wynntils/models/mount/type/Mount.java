/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.type.CappedValue;
import java.util.Map;

public record Mount(
        String name,
        int potential,
        String primaryColor,
        String secondaryColor,
        CappedValue currentEnergy,
        Map<MountStat, CappedValue> stats,
        Map<MountStat, Integer> maxStats,
        MountType mountType) {}