/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.RangedValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the identification data of an item.
 * <p>
 *     pendingCalculations is a map of stats to be processed when fully compiling an item,
 *     since we don't know the base values when decoding data (if we are not using extended encoding).
 * </p>
 */
public record IdentificationData(
        List<StatActualValue> identifications,
        Map<StatType, StatPossibleValues> possibleValues,
        boolean extendedEncoding,
        Map<StatType, Integer> pendingCalculations)
        implements ItemData {
    public static IdentificationData from(IdentifiableItemProperty<?, ?> property, boolean extendedEncoding) {
        return new IdentificationData(
                property.getIdentifications(),
                property.getPossibleValues().stream()
                        .collect(HashMap::new, (map, value) -> map.put(value.statType(), value), HashMap::putAll),
                extendedEncoding,
                Map.of());
    }

    public ErrorOr<Void> processPendingCalculations(Map<StatType, StatPossibleValues> possibleValuesMap) {
        for (Map.Entry<StatType, Integer> entry : pendingCalculations.entrySet()) {
            StatType statType = entry.getKey();
            int internalRoll = entry.getValue();

            StatPossibleValues possibleValues = possibleValuesMap.get(statType);
            if (possibleValues == null) {
                return ErrorOr.error("No possible values for stat type: " + statType);
            }

            int stars =
                    StatCalculator.calculateStarsFromInternalRoll(statType, possibleValues.baseValue(), internalRoll);
            int value = StatCalculator.calculateStatValue(internalRoll, possibleValues);

            identifications.add(
                    new StatActualValue(statType, value, stars, RangedValue.of(internalRoll, internalRoll)));
        }

        pendingCalculations.clear();

        return ErrorOr.of(null);
    }
}
