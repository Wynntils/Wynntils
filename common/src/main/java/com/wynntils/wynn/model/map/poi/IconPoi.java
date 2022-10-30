/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;

public abstract class IconPoi implements Poi {
    MapLocation location;

    protected IconPoi(MapLocation location) {
        this.location = location;
    }

    @Override
    public MapLocation getLocation() {
        return location;
    }

    @Override
    public int getWidth() {
        return getIcon().width();
    }

    @Override
    public int getHeight() {
        return getIcon().height();
    }

    public abstract Texture getIcon();

    @Override
    public void renderAt(
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {
        // TODO: This is really basic at the moment
        //       Add fading, and other configs

        float modifier = scale;

        if (hovered) {
            modifier *= 1.05;
        }

        Texture icon = getIcon();

        float width = icon.width() * modifier;
        float height = icon.height() * modifier;

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                icon.resource(),
                renderX - width / 2,
                renderZ - height / 2,
                0,
                width,
                height,
                icon.width(),
                icon.height());

        if (!hovered) return;

        // Render name if hovered

        poseStack.pushPose();

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        getName(),
                        renderX,
                        20 + renderZ,
                        CommonColors.GREEN,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);

        poseStack.popPose();
    }
}
