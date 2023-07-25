/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features.overlays;

import com.wynntils.utils.type.Pair;
import net.minecraft.world.phys.Vec2;

public enum Edge {
    TOP(Corner.TOP_LEFT, Corner.TOP_RIGHT),
    LEFT(Corner.TOP_LEFT, Corner.BOTTOM_LEFT),
    RIGHT(Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT),
    BOTTOM(Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT);

    private final Corner cornerA;
    private final Corner cornerB;

    Edge(Corner a, Corner b) {
        this.cornerA = a;
        this.cornerB = b;
    }

    public Corner getCornerA() {
        return cornerA;
    }

    public Corner getCornerB() {
        return cornerB;
    }

    public boolean isVerticalLine() {
        return this == LEFT || this == RIGHT;
    }

    public Pair<Vec2, Vec2> getEdgePos(Overlay overlay) {
        return new Pair<>(overlay.getCornerPoints(this.cornerA), overlay.getCornerPoints(this.cornerB));
    }
}
