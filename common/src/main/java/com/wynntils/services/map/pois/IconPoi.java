/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;

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

    private float getIconAlpha(float zoom) {
        float minZoom = getMinZoomForRender();
        if (minZoom <= -1) return 1f;

        return MathUtils.clamp(
                MathUtils.map(
                        zoom,
                        minZoom
                                * (1
                                        - Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                                .poiFadeAdjustment
                                                .get()),
                        minZoom,
                        0f,
                        1f),
                0f,
                1f);
    }

    protected CustomColor getIconColor() {
        return CommonColors.WHITE;
    }

    @Override
    public void renderAt(
            GuiGraphics guiGraphics,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float zoomRenderScale,
            float zoomLevel,
            boolean showLabels) {
        float modifier = scale;

        if (hovered) {
            modifier *= 1.05;
        }

        Texture icon = getIcon();

        float width = icon.width() * modifier;
        float height = icon.height() * modifier;

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                icon,
                this.getIconColor().withAlpha(this.getIconAlpha(zoomRenderScale)),
                renderX - width / 2,
                renderY - height / 2,
                width,
                height);

        if (hovered) {
            // Render name if hovered

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(getName()),
                            renderX,
                            15 + renderY,
                            CommonColors.GREEN,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE);
        }
    }

    @Override
    public boolean isVisible(float zoomRenderScale, float zoomLevel) {
        return this.getIconAlpha(zoomRenderScale) >= 0.1f;
    }
}
