/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;

public interface Poi {

    MapLocation getLocation();

    boolean hasStaticLocation();

    void renderAt(PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom);

    int getWidth();

    int getHeight();

    String getName();
}
