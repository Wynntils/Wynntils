/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;

public interface CountedItemProperty {
    int getCount();

    default boolean hasCount() {
        return true;
    }

    default CustomColor getCountColor() {
        return CommonColors.WHITE;
    }
}
