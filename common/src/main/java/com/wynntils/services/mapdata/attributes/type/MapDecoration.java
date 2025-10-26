/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import net.minecraft.client.gui.GuiGraphics;

// Allow dynamic map features to arbitrarily extend the rendering
public interface MapDecoration {
    MapDecoration NONE = new MapDecoration() {
        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public void render(GuiGraphics guiGraphics, boolean hovered, float zoomLevel) {}
    };

    boolean isVisible();

    void render(GuiGraphics guiGraphics, boolean hovered, float zoomLevel);
}
