/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties.type;

import com.wynntils.utils.objects.CustomColor;

public interface TextOverlayProperty extends PropertyType {

    TextOverlay getTextOverlay();

    boolean isTextOverlayEnabled();

    /** Whether this highlight should be shown in inventories */
    default boolean isInventoryText() {
        return true;
    }

    /** Whether this highlight should be shown in the hotbar */
    default boolean isHotbarText() {
        return false;
    }

    /**
     * Describes an item's text overlay, with its color, position relative to the item's slot, and text scale.
     */
    record TextOverlay(String text, CustomColor color, int xOffset, int yOffset, float scale) {

        public TextOverlay(String text, CustomColor color, int xOffset, int yOffset) {
            this(text, color, xOffset, yOffset, 1.0f);
        }
    }
}
