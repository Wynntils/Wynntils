/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import java.util.List;

public record GearInstance(List<StatActualValue> identifications, List<Powder> powders, int rerolls) {
    // FIXME

    /*
    DoubleSummaryStatistics percents = identifications.stream()
            .filter(Predicate.not(GearIdentificationContainer::isFixed))
            .mapToDouble(GearIdentificationContainer::percent)
            .summaryStatistics();
    overallPercentage = (float) percents.getAverage();
    if (percents.getCount() > 0) {
        // Only claim it is perfect/defective if we do have some non-fixed identifications
        isPerfect = overallPercentage >= 100f;
        isDefective = overallPercentage <= 0f;
        hasVariableIds = true;
    } else {
        isPerfect = false;
        isDefective = false;
        hasVariableIds = false;
    }

     */

    public boolean hasVariableIds() {
        return true;
    }

    public float getOverallPercentage() {
        return 33.3f;
    }

    public boolean isPerfect() {
        return false;
    }

    public boolean isDefective() {
        return false;
    }

    public StatActualValue getActualValue(StatType statType) {
        return identifications.stream()
                .filter(s -> s.stat().equals(statType))
                .findFirst()
                .orElse(null);
    }
}
