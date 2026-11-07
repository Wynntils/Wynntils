/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public interface CraftedItemProperty extends NamedItemProperty {
    List<StatType> getStatTypes();

    List<StatActualValue> getIdentifications();

    ClassType getRequiredClass();

    String getCreator();

    default float getOverallPercentage() {
        return StatCalculator.calculateOverallQualityFromVanillaMeters(getIdentifications());
    }

    default boolean hasFullPerfectInternalRolls() {
        return getIdentifications().stream()
                .filter(statActualValue -> statActualValue.vanillaMeter() != CappedValue.EMPTY)
                .allMatch(StatActualValue::perfectInternalRoll);
    }
}
