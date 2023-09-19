/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;

public record StatValue(StatPossibleValues possibleValues, StatActualValue statActualValue)
        implements Comparable<StatValue> {
    @Override
    public int compareTo(StatValue other) {
        if (statActualValue != null && other.statActualValue != null) {
            return Integer.compare(statActualValue.value(), other.statActualValue.value());
        } else if (statActualValue != null) {
            return -1;
        } else if (other.statActualValue != null) {
            return 1;
        } else {
            return Integer.compare(
                    possibleValues.range().high(), other.possibleValues.range().high());
        }
    }
}
