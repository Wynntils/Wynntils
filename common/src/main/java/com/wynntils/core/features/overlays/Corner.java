/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import java.util.List;

public enum Corner {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight;

    public List<Edge> getEdges() {
        return switch (this) {
            case TopLeft -> List.of(Edge.Top, Edge.Left);
            case TopRight -> List.of(Edge.Top, Edge.Right);
            case BottomLeft -> List.of(Edge.Bottom, Edge.Left);
            case BottomRight -> List.of(Edge.Bottom, Edge.Right);
        };
    }
}
