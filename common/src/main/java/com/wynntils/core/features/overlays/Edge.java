/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

public enum Edge {
    Top(Corner.TopLeft, Corner.TopRight),
    Left(Corner.TopLeft, Corner.BottomLeft),
    Right(Corner.TopRight, Corner.BottomRight),
    Bottom(Corner.BottomLeft, Corner.BottomRight);

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
}
