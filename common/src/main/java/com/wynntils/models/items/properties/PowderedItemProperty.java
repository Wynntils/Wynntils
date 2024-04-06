/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.elements.type.Powder;
import java.util.List;

public interface PowderedItemProperty {
    int getPowderSlots();

    List<Powder> getPowders();
}
