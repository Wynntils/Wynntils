/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.utils.type.Pair;
import java.util.List;

public record PowderData(int powderSlots, List<Pair<Powder, Integer>> powders) implements ItemData {
    public static PowderData from(PowderedItemProperty property) {
        // We don't know powder tiers, so we just assume they're all tier 6
        return new PowderData(
                property.getPowderSlots(),
                property.getPowders().stream()
                        .map(powder -> new Pair<>(powder, 6))
                        .toList());
    }
}
