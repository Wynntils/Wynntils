/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.RangedValue;

public record StatActualValue(
        StatType statType, int value, int stars, RangedValue internalRoll, boolean hasIconPrefix) {
    public StatActualValue(StatType statType, int value, int stars, RangedValue internalRoll) {
        this(statType, value, stars, internalRoll, false);
    }
}
