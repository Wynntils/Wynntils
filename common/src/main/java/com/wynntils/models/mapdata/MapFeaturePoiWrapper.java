/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.models.mapdata.type.features.MapLocation;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;

public class MapFeaturePoiWrapper implements Poi {
    public static final String FALLBACK_ICON_ID = "wynntils:icon:waypoint";
    private final MapFeature feature;
    private final MapFeatureAttributes attributes;

    public MapFeaturePoiWrapper(MapFeature feature) {
        this.feature = feature;
        attributes = Models.MapData.getAttributes(this.feature);
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

        String iconId = attributes.getIconId();
        if (iconId == null) {
            iconId = FALLBACK_ICON_ID;
        }
        ;

        MapFeatureIcon icon = Models.MapData.getIcon(iconId);

        float width = icon.width() * modifier;
        float height = icon.height() * modifier;

        CustomColor iconColor = attributes.getIconColor();
        if (iconColor == null) {
            iconColor = CommonColors.GRAY;
        }
        BufferedRenderUtils.drawColoredTexturedRect(
                poseStack,
                bufferSource,
                icon.getResourceLocation(),
                iconColor,
                this.getIconAlpha(mapZoom),
                renderX - width / 2,
                renderY - height / 2,
                getDisplayPriority().ordinal(), // z-index for rendering
                width,
                height);

        if (hovered && attributes.getLabel() != null) {
            // Render name if hovered

            poseStack.pushPose();

            CustomColor labelColor = attributes.getLabelColor();
            if (labelColor == null) {
                labelColor = CommonColors.GRAY;
            }
            TextShadow labelShadow = attributes.getLabelShadow();
            if (labelShadow == null) {
                labelShadow = TextShadow.NONE;
            }
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(attributes.getLabel()),
                            renderX,
                            15 + renderY,
                            labelColor,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            labelShadow);

            poseStack.popPose();
        }
    }

    private float getIconAlpha(float mapZoom) {
        // FIXME: Depend on icon visibility
        return 1f;
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        String iconId = attributes.getIconId();
        if (iconId == null) return 32;

        MapFeatureIcon icon = Models.MapData.getIcon(iconId);

        return (int) (icon.width() * scale);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        String iconId = attributes.getIconId();
        if (iconId == null) return 32;

        MapFeatureIcon icon = Models.MapData.getIcon(iconId);

        return (int) (icon.height() * scale);
    }

    @Override
    public String getName() {
        return "Wrapped MapFeature [" + feature.getFeatureId() + "]";
    }
}
