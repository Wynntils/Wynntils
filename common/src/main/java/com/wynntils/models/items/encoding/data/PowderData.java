/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public record PowderData(
        int powderSlots, List<Pair<Powder, Integer>> powders, Optional<PowderSpecialTierInfo> powderSpecial)
        implements ItemData {
    public static PowderData from(PowderedItemProperty property) {
        return new PowderData(property.getPowderSlots(), property.getPowders(), property.getPowderSpecial());
    }
}
