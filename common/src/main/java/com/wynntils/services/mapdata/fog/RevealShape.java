/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.fog;

public enum RevealShape {
    SQUARE,
    CIRCLE;

    boolean contains(int offsetX, int offsetZ, int radius) {
        return this == SQUARE || offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }
}
