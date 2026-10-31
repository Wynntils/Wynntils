/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

public enum TooltipAlignment {
    LEFT,
    CENTER,
    RIGHT;

    public int offset(int containerWidth, int width) {
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> (containerWidth - width) / 2;
            case RIGHT -> containerWidth - width;
        };
    }
}
