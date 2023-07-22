/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
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
            PoseStack poseStack,
            MultiBufferSource bufferSource,
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

        // zoom 3 (max zoomed in) up until 0.8 will keep service poi at 100% alpha (1.0f)
        // fading until we get to zoom 0.540, at which point it goes to 0.1f and disappears.
        // and finally at zoom 0.4727, it goes to 0.0f

        // fast travel is at 100% until 0.1667, and disappareas with 0.1f at:
        // 0.10858048, or possibly 0.11261813, and completely with 0.0f at 0.1,
        // which is max zoomed out

        // these happense since:
        // public final Config<Float> servicePoiMinZoom = new Config<>(0.8f);
        // public final Config<Float> fastTravelPoiMinZoom = new Config<>(0.166f);

        // so this point shows when it starts to fade, but we want to specify
        // the opposite, when it should be totally gone.
        // and then also possibly a fading speed...

        // we also have
        // public final Config<Float> poiFadeAdjustment = new Config<>(0.4f);
        // which is used to calculate where the item is completely faded out:
        // minZoom * (1 - poiFadeAdjustment),

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
                            StyledText.fromString(getName()),
                            renderX,
                            15 + renderY,
                            CommonColors.GREEN,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE);

            poseStack.popPose();
        }
    }
}
