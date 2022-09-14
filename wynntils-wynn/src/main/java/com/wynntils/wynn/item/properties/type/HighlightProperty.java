/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties.type;

import com.wynntils.mc.objects.CustomColor;

public interface HighlightProperty extends PropertyType {

    CustomColor getHighlightColor();

    boolean isHighlightEnabled();

    /** Whether this highlight should be shown in inventories */
    default boolean isInventoryHighlight() {
        return true;
    }

    /** Whether this highlight should be shown in the hotbar */
    default boolean isHotbarHighlight() {
        return true;
    }
}
