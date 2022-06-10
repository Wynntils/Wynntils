/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.render;

import com.wynntils.utils.objects.CustomColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;

public interface HighlightedItem {
    CustomColor getHighlightColor(Screen screen, Slot slot);
}
