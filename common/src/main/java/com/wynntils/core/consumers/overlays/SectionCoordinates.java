/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

public record SectionCoordinates(int x1, int y1, int x2, int y2) {
    public boolean overlaps(float x, float y) {
        return (x1 <= x && x2 >= x) && (y1 <= y && y2 >= y);
    }
}
