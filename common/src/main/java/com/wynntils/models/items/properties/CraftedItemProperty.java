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

public interface CraftedItemProperty extends NamedItemProperty {
    List<StatType> getStatTypes();

    List<StatActualValue> getIdentifications();

    List<StatPossibleValues> getPossibleValues();

    ClassType getRequiredClass();
}
