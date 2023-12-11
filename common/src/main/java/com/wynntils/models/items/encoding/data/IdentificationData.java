/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record IdentificationData(
        List<StatActualValue> identifications, Map<StatType, StatPossibleValues> possibleValues) implements ItemData {
    public static IdentificationData from(IdentifiableItemProperty property) {
        return new IdentificationData(
                property.getIdentifications(),
                property.getPossibleValues().stream()
                        .collect(HashMap::new, (map, value) -> map.put(value.statType(), value), HashMap::putAll));
    }
}
