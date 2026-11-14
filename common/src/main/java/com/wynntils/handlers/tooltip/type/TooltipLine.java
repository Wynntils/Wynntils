/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import java.util.List;
import net.minecraft.network.chat.Component;

public record TooltipLine(List<TooltipLinePart> parts) implements TooltipElement {
    private static final TooltipLine EMPTY = new TooltipLine(List.of());

    public TooltipLine {
        parts = List.copyOf(parts);
    }

    public static TooltipLine of(TooltipLinePart... parts) {
        return new TooltipLine(List.of(parts));
    }

    public static TooltipLine empty() {
        return EMPTY;
    }

    public static TooltipLine left(Component component) {
        return of(TooltipLinePart.left(component));
    }

    public static TooltipLine center(Component component) {
        return of(TooltipLinePart.center(component));
    }

    public static TooltipLine right(Component component) {
        return of(TooltipLinePart.right(component));
    }

    public static TooltipLine leftRight(Component left, Component right) {
        return of(TooltipLinePart.left(left), TooltipLinePart.right(right));
    }
}
