/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties.type;

import com.wynntils.gui.render.TextRenderTask;

public interface TextOverlayProperty extends PropertyType {

    TextOverlay getTextOverlay();

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

    /**
     * Describes an item's text overlay, with its color, position relative to the item's slot, and text scale.
     */
    record TextOverlay(TextRenderTask task, int xOffset, int yOffset, float scale) {}
}
