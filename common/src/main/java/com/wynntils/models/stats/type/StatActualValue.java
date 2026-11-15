/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.RangedValue;

public record StatActualValue(
        StatType statType, int value, RangedValue internalRoll, boolean perfectInternalRoll, CappedValue vanillaMeter) {
    public StatActualValue(StatType statType, int value, RangedValue internalRoll, boolean perfectInternalRoll) {
        this(statType, value, internalRoll, perfectInternalRoll, CappedValue.EMPTY);
    }
}
