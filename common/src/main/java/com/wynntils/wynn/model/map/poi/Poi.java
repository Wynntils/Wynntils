/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

public abstract class Poi {
    private final MapLocation location;

    public Poi(MapLocation location) {
        this.location = location;
    }

    public MapLocation getLocation() {
        return location;
    }

    public abstract Texture getIcon();

    public abstract String getName();
}
