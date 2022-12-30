/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties.type;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;

public interface TextOverlayProperty extends PropertyType {

    ItemTextOverlayFeature.TextOverlay getTextOverlay();

    boolean isTextOverlayEnabled();

    /**
     * Whether this overlay is allowed to be rendered in inventories.
     */
    default boolean isInventoryText() {
        return true;
    }

    /**
     * Whether this overlay is allowed to be rendered in the hotbar.
     */
    default boolean isHotbarText() {
        return true;
    }
}
