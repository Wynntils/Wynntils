/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface ItemTooltipProvider {
    void renderHoveredItemTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);
}
