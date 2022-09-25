/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

public abstract class Poi {
    private MapLocation location;

    public Poi(MapLocation location) {
        this.location = location;
    }

    public MapLocation getLocation() {
        return location;
    }

    public abstract Texture getIcon();

    public abstract String getName();

    /**
     * {@param mapCenterX} center coordinates of map (in-game coordinates)
     * {@param centerX} center coordinates of map (screen render coordinates)
     * {@param currentZoom} the bigger, the more detailed the map is
     */
    public float getRenderX(float mapCenterX, float centerX, float currentZoom) {
        double distanceX = this.getLocation().getX() - mapCenterX;
        return (float) (centerX + distanceX * currentZoom);
    }

    /**
     * {@param mapCenterZ} center coordinates of map (in-game coordinates)
     * {@param centerZ} center coordinates of map (screen render coordinates)
     * {@param currentZoom} the bigger, the more detailed the map is
     */
    public float getRenderZ(float mapCenterZ, float centerZ, float currentZoom) {
        double distanceZ = this.getLocation().getZ() - mapCenterZ;
        return (float) (centerZ + distanceZ * currentZoom);
    }
}
