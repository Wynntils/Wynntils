/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.type.DisplayPriority;
import net.minecraft.client.renderer.MultiBufferSource;

public interface Poi {
    PoiLocation getLocation();

    /**
     * Display priority is used to determine the order in which POIs are rendered.
     * A higher display priority means, that the POI is rendered later, so it will be on top of other POIs.
     */
    DisplayPriority getDisplayPriority();

    boolean hasStaticLocation();

    void renderAt(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float mapZoom);

    int getWidth(float mapZoom, float scale);

    int getHeight(float mapZoom, float scale);

    String getName();
}
