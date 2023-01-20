/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.user.map.MapFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.render.buffered.BufferedRenderUtils;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.MathUtils;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class IconPoi implements Poi {
    @Override
    public int getWidth(float mapZoom, float scale) {
        return (int) (getIcon().width() * scale);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        return (int) (getIcon().height() * scale);
    }

    public abstract Texture getIcon();

    // Returns the minimum zoom where the poi should be rendered with full alpha
    // Return -1 to always render without fading
    protected abstract float getMinZoomForRender();

    public float getIconAlpha(float zoom) {
        if (getMinZoomForRender() <= -1) return 1f;

        return MathUtils.clamp(
                MathUtils.map(
                        zoom,
                        getMinZoomForRender() - MapFeature.INSTANCE.poiFadeDistance,
                        getMinZoomForRender(),
                        0f,
                        1f),
                0f,
                1f);
    }

    public CustomColor getIconColor() {
        return CommonColors.WHITE;
    }

    @Override
    public void renderAt(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float mapZoom) {
        float modifier = scale;

        if (hovered) {
            modifier *= 1.05;
        }

        Texture icon = getIcon();

        float width = icon.width() * modifier;
        float height = icon.height() * modifier;

        BufferedRenderUtils.drawColoredTexturedRect(
                poseStack,
                bufferSource,
                icon.resource(),
                this.getIconColor(),
                this.getIconAlpha(mapZoom),
                renderX - width / 2,
                renderY - height / 2,
                getDisplayPriority().ordinal(), // z-index for rendering
                width,
                height);

        if (hovered) {
            // Render name if hovered

            poseStack.pushPose();

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            getName(),
                            renderX,
                            20 + renderY,
                            CommonColors.GREEN,
                            HorizontalAlignment.Center,
                            VerticalAlignment.Middle,
                            TextShadow.OUTLINE);

            poseStack.popPose();
        }
    }
}
