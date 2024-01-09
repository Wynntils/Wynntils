/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.IdentifiableItemProperty;

public record NameData(String name) implements ItemData {
    public static NameData from(IdentifiableItemProperty property) {
        return new NameData(property.getName());
    }
}
