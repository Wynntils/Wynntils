/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.mc.type.PoiLocation;
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
