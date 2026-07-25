/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.RangedValue;
import java.util.Optional;

public record StatActualValue(
        StatType statType,
        int value,
        int stars,
        RangedValue internalRoll,
        boolean hasIconPrefix,
        Optional<Character> vanillaMeter) {
    public StatActualValue(StatType statType, int value, int stars, RangedValue internalRoll) {
        this(statType, value, stars, internalRoll, false, Optional.empty());
    }

    public StatActualValue(StatType statType, int value, int stars, RangedValue internalRoll, boolean hasIconPrefix) {
        this(statType, value, stars, internalRoll, hasIconPrefix, Optional.empty());
    }
}
