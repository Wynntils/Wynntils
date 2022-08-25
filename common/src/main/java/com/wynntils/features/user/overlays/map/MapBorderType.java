/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays.map;

import com.wynntils.mc.render.Texture;

public enum MapBorderType {
    Gilded(Texture.GILDED_MAP_TEXTURES, 0, 262, 262, 524, 1),
    Paper(Texture.PAPER_MAP_TEXTURES, 0, 0, 217, 217, 3),
    Wynn(Texture.WYNN_MAP_TEXTURES, 0, 0, 112, 112, 3);
    private final Texture texture;
    private final int tx1;
    private final int ty1;
    private final int tx2;
    private final int ty2;
    private final int groovesSize;

    MapBorderType(Texture texture, int tx1, int ty1, int tx2, int ty2, int groovesSize) {
        this.texture = texture;
        this.tx1 = tx1;
        this.ty1 = ty1;
        this.tx2 = tx2;
        this.ty2 = ty2;
        this.groovesSize = groovesSize;
    }

    public Texture texture() {
        return texture;
    }

    public int tx1() {
        return tx1;
    }

    public int ty1() {
        return ty1;
    }

    public int tx2() {
        return tx2;
    }

    public int ty2() {
        return ty2;
    }

    public int groovesSize() {
        return groovesSize;
    }
}
