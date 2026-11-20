/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import net.minecraft.network.chat.Component;

public record TooltipLinePart(Component component, TooltipAlignment alignment) {
    public static TooltipLinePart left(Component component) {
        return new TooltipLinePart(component, TooltipAlignment.LEFT);
    }

    public static TooltipLinePart center(Component component) {
        return new TooltipLinePart(component, TooltipAlignment.CENTER);
    }

    public static TooltipLinePart right(Component component) {
        return new TooltipLinePart(component, TooltipAlignment.RIGHT);
    }
}
