/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.utils.type.RangedValue;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public record StatCalculationInfo(
        RangedValue range,
        RoundingMode roundingMode,
        Optional<Integer> minimumValue,
        Optional<Integer> maximumValue,
        List<RangedValue> starInternalRollRanges) {}
