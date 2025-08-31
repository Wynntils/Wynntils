/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.attributes.type.ResolvedMapAttributes;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;

public class MapFeaturePoiWrapper implements Poi {
    public static final int SPACING = 2;
    private static final float TEXT_SCALE = 1f;
    private final MapFeature feature;
    private final ResolvedMapAttributes attributes;

    public MapFeaturePoiWrapper(MapFeature feature, ResolvedMapAttributes attributes) {
        this.feature = feature;
        this.attributes = attributes;
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
    public boolean isVisible(float zoomRenderScale, float zoomLevel) {
        float iconAlpha = Services.MapData.calculateVisibility(attributes.iconVisibility(), zoomLevel);
        Optional<MapIcon> icon = Services.MapData.getIcon(attributes.iconId());
        if (icon.isPresent() && iconAlpha > 0.01) {
            return true;
        }

        float labelAlpha = Services.MapData.calculateVisibility(attributes.labelVisibility(), zoomLevel);
        if (!attributes.label().isEmpty() && labelAlpha > 0.01) {
            return true;
        }

        return false;
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        Optional<MapIcon> icon = Services.MapData.getIcon(attributes.iconId());
        if (icon.isPresent()) {
            return (int) (icon.get().getWidth() * scale);
        }

        if (!attributes.label().isEmpty()) {
            // Use label for measurements
            return (int) (FontRenderer.getInstance().getFont().width(attributes.label()) * scale);
        }

        // No icon or label, return 32 as fallback
        WynntilsMod.warn("No icon or label for feature " + feature.getFeatureId());
        return 32;
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        Optional<MapIcon> icon = Services.MapData.getIcon(attributes.iconId());
        if (icon.isPresent()) {
            return (int) (icon.get().getHeight() * scale);
        }

        if (!attributes.label().isEmpty()) {
            // Use label for measurements
            return (int) (FontRenderer.getInstance().getFont().lineHeight * scale * TEXT_SCALE);
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
            float zoomLevel,
            boolean showLabels) {
        MapFeatureRenderer.renderMapFeature(
                poseStack, bufferSource, feature, attributes, renderX, renderY, hovered, scale, zoomLevel, showLabels);
    }
}
