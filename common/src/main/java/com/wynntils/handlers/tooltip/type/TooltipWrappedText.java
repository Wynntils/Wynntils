/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import net.minecraft.network.chat.Component;

public record TooltipWrappedText(Component component, TooltipAlignment alignment, int maxWidth)
        implements TooltipElement {
    public TooltipWrappedText {
        if (maxWidth <= 0) {
            throw new IllegalArgumentException("Maximum width must be positive");
        }
    }

    public TooltipWrappedText(Component component, TooltipAlignment alignment) {
        this(component, alignment, Integer.MAX_VALUE);
    }

    public static TooltipWrappedText left(Component component) {
        return new TooltipWrappedText(component, TooltipAlignment.LEFT);
    }

    public static TooltipWrappedText left(Component component, int maxWidth) {
        return new TooltipWrappedText(component, TooltipAlignment.LEFT, maxWidth);
    }

    public static TooltipWrappedText center(Component component) {
        return new TooltipWrappedText(component, TooltipAlignment.CENTER);
    }

    public static TooltipWrappedText center(Component component, int maxWidth) {
        return new TooltipWrappedText(component, TooltipAlignment.CENTER, maxWidth);
    }

    public static TooltipWrappedText right(Component component) {
        return new TooltipWrappedText(component, TooltipAlignment.RIGHT);
    }

    public static TooltipWrappedText right(Component component, int maxWidth) {
        return new TooltipWrappedText(component, TooltipAlignment.RIGHT, maxWidth);
    }
}
