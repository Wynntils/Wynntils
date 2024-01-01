/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.stats.type.ShinyStat;

public record ShinyData(ShinyStat shinyStat) implements ItemData {
    public static ShinyData from(ShinyItemProperty property) {
        return new ShinyData(property.getShinyStat().orElse(null));
    }
}
