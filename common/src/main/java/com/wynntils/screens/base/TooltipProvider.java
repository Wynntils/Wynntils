/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import java.util.List;
import net.minecraft.network.chat.Component;

public interface TooltipProvider {
    List<Component> getTooltipLines();
}
