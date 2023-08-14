/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.utils.type.CappedValue;

public interface UsesItemPropery extends CountedItemProperty {
    CappedValue getUses();

    @Override
    default int getCount() {
        CappedValue value = getUses();
        if (value == null) return 0;
        return value.current();
    }

    @Override
    default boolean hasCount() {
        return getUses() != null;
    }
}
