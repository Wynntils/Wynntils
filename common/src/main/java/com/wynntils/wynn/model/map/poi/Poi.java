/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
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
            MultiBufferSource.BufferSource bufferSource,
            float renderX,
            float renderZ,
            boolean hovered,
            float scale,
            float mapZoom);

    int getWidth(float mapZoom, float scale);

    int getHeight(float mapZoom, float scale);

    String getName();
}
