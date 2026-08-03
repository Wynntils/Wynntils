/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import java.util.List;

public record CustomIdentificationsData(List<StatPossibleValues> possibleValues, List<StatActualValue> identifications)
        implements ItemData {
    public CustomIdentificationsData(List<StatPossibleValues> possibleValues) {
        this(possibleValues, List.of());
    }
}
