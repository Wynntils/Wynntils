/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.render;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;

public interface TextOverlayItem {
    List<TextOverlay> getOverlays(Screen screen, Slot slot);

    record TextOverlay(String text, int color, int xOffset, int yOffset) {}
}
