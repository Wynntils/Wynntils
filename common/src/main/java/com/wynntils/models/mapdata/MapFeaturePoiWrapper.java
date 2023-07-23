/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapIcon;
import com.wynntils.models.mapdata.type.MapFeature;
import com.wynntils.models.mapdata.type.MapLocation;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;

public class MapFeaturePoiWrapper implements Poi {
    public static final String FALLBACK_ICON_ID = "wynntils:icon:symbols:waypoint";
    private final MapFeature feature;
    private final MapAttributes attributes;

    public MapFeaturePoiWrapper(MapFeature feature) {
        this.feature = feature;
        attributes = Models.MapData.getFullFeatureAttributes(this.feature);
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
        float labelAlpha = 0.9f;

        if (hovered) {
            modifier *= 1.05;
            labelAlpha = 1.0f;
        }

        String iconId = attributes.getIconId();
        if (hasIcon(iconId)) {
            MapIcon icon = Models.MapData.getIcon(iconId);

            float width = icon.width() * modifier;
            float height = icon.height() * modifier;

            CustomColor iconColor = attributes.getIconColor();
            if (iconColor == null) {
                iconColor = CommonColors.WHITE;
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
        }

        String label = attributes.getLabel();

        if (hasLabel(label)) {
            // If we have an icon, draw label below
            int yOffset = hasIcon(iconId) ? 15 : 0;

            poseStack.pushPose();
            poseStack.translate(renderX, renderY + yOffset, getDisplayPriority().ordinal());
            poseStack.scale(modifier, modifier, modifier);

            CustomColor labelColor = attributes.getLabelColor();
            if (labelColor == null) {
                labelColor = CommonColors.WHITE;
            }
            TextShadow labelShadow = attributes.getLabelShadow();
            if (labelShadow == null) {
                labelShadow = TextShadow.OUTLINE;
            }
            CustomColor color = labelColor.withAlpha(labelAlpha);

            BufferedFontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            bufferSource,
                            StyledText.fromString(label),
                            0,
                            0,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            labelShadow,
                            1f);
            if (hovered) {
                int cLevel = attributes.getLevel();
                if (cLevel != 0) {
                    BufferedFontRenderer.getInstance()
                            .renderText(
                                    poseStack,
                                    bufferSource,
                                    StyledText.fromString("[Lv " + cLevel + "]"),
                                    0,
                                    10,
                                    color,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.MIDDLE,
                                    labelShadow,
                                    1f);
                }
            }

            poseStack.popPose();
        }
    }

    private boolean hasIcon(String iconId) {
        return !(iconId == null || iconId.equals(MapIcon.NO_ICON_ID));
    }

    private boolean hasLabel(String label) {
        return label != null && !label.isEmpty();
    }

    private float getIconAlpha(float mapZoom) {
        // FIXME: Depend on icon visibility
        return 1f;
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        String iconId = attributes.getIconId();
        String label = attributes.getLabel();

        if (!hasIcon(iconId) && hasLabel(label)) {
            // Use label for measurements
            return (int) (FontRenderer.getInstance().getFont().width(attributes.getLabel()) * scale);
        }

        MapIcon icon = Models.MapData.getIcon(iconId);
        if (icon == null) return 32;

        return (int) (icon.width() * scale);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        String iconId = attributes.getIconId();
        String label = attributes.getLabel();

        if (!hasIcon(iconId) && hasLabel(label)) {
            // Use label for measurements
            return (int) (FontRenderer.getInstance().getFont().lineHeight * scale);
        }

        MapIcon icon = Models.MapData.getIcon(iconId);
        if (icon == null) return 32;

        return (int) (icon.height() * scale);
    }

    @Override
    public String getName() {
        return "Wrapped MapFeature [" + feature.getFeatureId() + "]";
    }
}
