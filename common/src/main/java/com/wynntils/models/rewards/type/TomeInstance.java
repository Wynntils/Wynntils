/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;
import java.util.Optional;

public record TomeInstance(int rerolls, List<StatActualValue> identifications, Optional<Float> overallQuality) {
    public static TomeInstance create(int rerolls, TomeInfo tomeInfo, List<StatActualValue> identifications) {
        return new TomeInstance(
                rerolls,
                identifications,
                StatCalculator.calculateOverallQuality(
                        tomeInfo.name(), tomeInfo.getPossibleValueList(), identifications));
    }

    public boolean hasOverallValue() {
        return overallQuality.isPresent();
    }

    public float getOverallPercentage() {
        return overallQuality.orElse(0.0f);
    }

    public boolean isPerfect() {
        return overallQuality.orElse(0.0f) >= 100.0f;
    }

    public boolean isDefective() {
        return overallQuality.isPresent() && overallQuality.orElse(0.0f) <= 0.0f;
    }
}
