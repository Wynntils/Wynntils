/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public interface CountedItemProperty {
    int getCount();

    default boolean hasCount() {
        return true;
    }

    default CustomColor getCountColor() {
        return CommonColors.WHITE;
    }
}
