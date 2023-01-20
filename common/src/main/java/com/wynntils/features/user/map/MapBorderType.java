/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.map;

import com.wynntils.utils.render.Texture;

public enum MapBorderType {
    Gilded(Texture.GILDED_MAP_TEXTURES, new BorderInfo(0, 262, 262, 524), new BorderInfo(0, 0, 262, 262), 1),
    Paper(Texture.PAPER_MAP_TEXTURES, new BorderInfo(0, 0, 217, 217), new BorderInfo(0, 217, 217, 438), 3),
    Wynn(Texture.WYNN_MAP_TEXTURES, new BorderInfo(0, 0, 112, 112), new BorderInfo(0, 112, 123, 235), 3);
    private final Texture texture;
    private final BorderInfo square;
    private final BorderInfo circle;
    private final int groovesSize;

    MapBorderType(Texture texture, BorderInfo square, BorderInfo circle, int groovesSize) {
        this.texture = texture;
        this.square = square;
        this.circle = circle;
        this.groovesSize = groovesSize;
    }

    public Texture texture() {
        return texture;
    }

    public int groovesSize() {
        return groovesSize;
    }

    public BorderInfo square() {
        return square;
    }

    public BorderInfo circle() {
        return circle;
    }

    public record BorderInfo(int tx1, int ty1, int tx2, int ty2) {}
}
