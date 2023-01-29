/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;

public class GearCalculator {
    public static float getPercent(StatActualValue actualValue, StatPossibleValues possibleValues) {
        StatType statType = actualValue.stat();
        int max = possibleValues.range().high() - possibleValues.range().low();
        int current = actualValue.value() - possibleValues.range().low();

        return (float) current / max * 100.0f;
    }
}
