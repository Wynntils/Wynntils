/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class Poi {
    private final MapLocation location;

    protected Poi(MapLocation location) {
        this.location = location;
    }

    public MapLocation getLocation() {
        return location;
    }

    public abstract void renderAt(PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale);

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract String getName();
}
