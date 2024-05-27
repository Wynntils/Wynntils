/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;

public class MapFeaturePoiWrapper implements Poi {
    public static final int SPACING = 2;
    public static final float TEXT_SCALE = 1f;
    private final MapFeature feature;
    private final MapAttributes attributes;

    public MapFeaturePoiWrapper(MapFeature feature) {
        this.feature = feature;
        attributes = Services.MapData.getFullFeatureAttributes(this.feature);
    }

    @Override
    public PoiLocation getLocation() {
        if (feature instanceof MapLocation mapLocation) {
            return PoiLocation.fromLocation(mapLocation.getLocation());
        }

        return null;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.HIGHEST;
    }

    @Override
    public boolean hasStaticLocation() {
        return false;
    }

    @Override
    public String getName() {
        return "Wrapped MapFeature [" + feature.getFeatureId() + "]";
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        Optional<MapIcon> icon =
                Services.MapData.getIcon(attributes.getIconId().get());
        if (icon.isPresent()) {
            return (int) (icon.get().getWidth() * scale);
        }

        if (!attributes.getLabel().get().isEmpty()) {
            // Use label for measurements
            return (int) (FontRenderer.getInstance()
                            .getFont()
                            .width(attributes.getLabel().get())
                    * scale);
        }

        // No icon or label, return 32 as fallback
        WynntilsMod.warn("No icon or label for feature " + feature.getFeatureId());
        return 32;
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        Optional<MapIcon> icon =
                Services.MapData.getIcon(attributes.getIconId().get());
        if (icon.isPresent()) {
            return (int) (icon.get().getHeight() * scale);
        }

        if (!attributes.getLabel().get().isEmpty()) {
            // Use label for measurements
            return getLabelHeight(scale);
        }

        // No icon or label, return 32 as fallback
        WynntilsMod.warn("No icon or label for feature " + feature.getFeatureId());
        return 32;
    }

    @Override
    public void renderAt(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float zoomRenderScale,
            float zoomLevel) {
        float renderScale = hovered ? scale * 1.05f : scale;
        // this is the default alpha for labels
        float hoverAlphaFactor = hovered ? 1f : 0.9f;
        int labelHeight = getLabelHeight(renderScale);

        int yOffset = 0;

        poseStack.pushPose();
        // z-index for rendering
        poseStack.translate(renderX, renderY, getDisplayPriority().ordinal());
        poseStack.scale(renderScale, renderScale, renderScale);

        // Draw icon, if applicable
        float iconAlpha = hoverAlphaFactor * this.getIconAlpha(zoomLevel);
        Optional<MapIcon> icon = Services.MapData.getIcon(attributes.getIconId().get());
        boolean drawIcon = iconAlpha > 0.01;
        if (icon.isPresent() && drawIcon) {
            int iconWidth = icon.get().getWidth();
            int iconHeight = icon.get().getHeight();

            if (hovered) {
                // If this is hovered, show with full alpha
                iconAlpha = 1f;
            }

            BufferedRenderUtils.drawColoredTexturedRect(
                    poseStack,
                    bufferSource,
                    icon.get().getResourceLocation(),
                    attributes.getIconColor().get(),
                    iconAlpha,
                    0 - iconWidth / 2f,
                    yOffset - iconHeight / 2f,
                    0,
                    iconWidth,
                    iconHeight);
            yOffset += (iconHeight + labelHeight) / 2 + SPACING;
        }

        // Draw label, if applicable
        float labelAlpha = hoverAlphaFactor * getLabelAlpha(zoomLevel);
        // Small enough alphas are turned into 255, so trying to render them results in
        // visual glitches
        // Always draw labels for hovered icons, regardless of label visibility rules
        boolean drawLabel = labelAlpha > 0.01 || (drawIcon && hovered);
        if (!attributes.getLabel().get().isEmpty() && drawLabel) {
            if (drawIcon && hovered) {
                // If this is hovered, show with full alpha
                labelAlpha = 1f;
            }

            CustomColor color = attributes.getLabelColor().get().withAlpha(labelAlpha);

            BufferedFontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            bufferSource,
                            StyledText.fromString(attributes.getLabel().get()),
                            0,
                            yOffset,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            attributes.getLabelShadow().get(),
                            TEXT_SCALE);
            yOffset += labelHeight + SPACING;
        }

        // Draw level, if applicable
        Optional<Integer> level = attributes.getLevel();
        // Show level only for features that are displayed and hovered
        boolean drawLevel = hovered && (drawIcon || drawLabel);
        if (level.isPresent() && level.get() >= 1 && drawLevel) {
            BufferedFontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            bufferSource,
                            StyledText.fromString("[Lv. " + level.get() + "]"),
                            0,
                            yOffset,
                            attributes.getLabelColor().get(),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            attributes.getLabelShadow().get(),
                            TEXT_SCALE);
        }

        poseStack.popPose();
    }

    private float getIconAlpha(float zoomLevel) {
        Optional<MapVisibility> iconVisibility = attributes.getIconVisibility();
        // If no visibility is specified, always show
        if (iconVisibility.isEmpty()) {
            return 1f;
        }

        return Services.MapData.getVisibilityForZoomLevel(iconVisibility, zoomLevel);
    }

    private float getLabelAlpha(float zoomLevel) {
        Optional<MapVisibility> labelVisibility = attributes.getLabelVisibility();
        // If no visibility is specified, always show
        if (labelVisibility.isEmpty()) {
            return 1f;
        }

        return Services.MapData.getVisibilityForZoomLevel(labelVisibility, zoomLevel);
    }

    private int getLabelHeight(float scale) {
        return (int) (FontRenderer.getInstance().getFont().lineHeight * scale * TEXT_SCALE);
    }
}
