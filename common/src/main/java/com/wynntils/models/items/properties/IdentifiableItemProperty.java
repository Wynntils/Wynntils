/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import java.util.List;

/**
 * Represents an item that can be identified, and provides the information needed to build a tooltip for it.
 */
public interface IdentifiableItemProperty {
    String getName();

    ClassType getRequiredClass();

    List<StatType> getVariableStats();

    List<StatActualValue> getIdentifications();

    List<StatPossibleValues> getPossibleValues();

    boolean hasOverallValue();

    boolean isPerfect();

    boolean isDefective();

    float getOverallPercentage();
}
