/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

/**
 * Represents an item that can be identified, and provides the information needed to build a tooltip for it.
 * @param <T> The type of the item info
 * @param <U> The type of the item instance
 */
public interface IdentifiableItemProperty<T, U> extends NamedItemProperty {
    T getItemInfo();

    Optional<U> getItemInstance();

    boolean isUnidentified();

    ClassType getRequiredClass();

    List<StatType> getVariableStats();

    List<StatActualValue> getIdentifications();

    List<StatPossibleValues> getPossibleValues();

    RangedValue getIdentificationLevelRange();

    boolean hasOverallValue();

    boolean isPerfect();

    boolean isDefective();

    float getOverallPercentage();

    default boolean hasEstimatedOverallPercentage() {
        return getIdentifications().stream().anyMatch(actual -> {
            if (actual.vanillaMeter() == CappedValue.EMPTY) return false;
            return getPossibleValues().stream()
                    .filter(possible -> possible.statType().equals(actual.statType()))
                    .findFirst()
                    .map(possible -> !possible.range().inRange(actual.value()))
                    .orElse(true);
        });
    }

    default boolean hasFullPerfectInternalRolls() {
        return getIdentifications().stream()
                .filter(statActualValue -> statActualValue.vanillaMeter() != CappedValue.EMPTY)
                .allMatch(StatActualValue::perfectInternalRoll);
    }
}
