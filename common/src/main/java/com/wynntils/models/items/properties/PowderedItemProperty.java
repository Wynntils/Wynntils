/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public interface PowderedItemProperty {
    int getPowderSlots();

    List<Pair<Powder, Integer>> getPowders();

    Optional<PowderSpecialTierInfo> getPowderSpecial();
}
