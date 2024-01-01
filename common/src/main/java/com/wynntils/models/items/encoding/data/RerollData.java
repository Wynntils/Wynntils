/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.RerollableItemProperty;

public record RerollData(int rerolls) implements ItemData {
    public static RerollData from(RerollableItemProperty property) {
        return new RerollData(property.getRerollCount());
    }
}
