/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import java.util.List;
import net.minecraft.network.chat.Component;

public interface TooltipProvider {
    List<Component> getTooltipLines();
}
