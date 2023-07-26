/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import java.util.List;

public enum Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    public List<Edge> getEdges() {
        return switch (this) {
            case TOP_LEFT -> List.of(Edge.TOP, Edge.LEFT);
            case TOP_RIGHT -> List.of(Edge.TOP, Edge.RIGHT);
            case BOTTOM_LEFT -> List.of(Edge.BOTTOM, Edge.LEFT);
            case BOTTOM_RIGHT -> List.of(Edge.BOTTOM, Edge.RIGHT);
        };
    }
}
