/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2f;
import org.joml.Vector2i;

public final class MapFeatureRenderer {
    private static final int SPACING = 2;
    private static final float TEXT_SCALE = 1f;

    // Small/Large enough alphas are turned into 0/255, so trying to render them results in visual glitches
    // Generally <0.1f -> 0f, >0.9f -> 1f
    private static final float MINIMUM_RENDER_ALPHA = 0.1f;

    public static void renderMapFeature(
            GuiGraphics guiGraphics,
            MapFeature feature,
            ResolvedMapAttributes attributes,
            Vector2f mapCenter,
            Vector2f screenCenter,
            Vector2f rotationVector,
            float zoomLevel,
            float zoomRenderScale,
            float featureRenderScale,
            boolean hovered,
            boolean fullscreenMap) {
        if (feature instanceof MapLocation location) {
            renderMapLocation(
                    guiGraphics,
                    location,
                    attributes,
                    mapCenter,
                    screenCenter,
                    rotationVector,
                    zoomLevel,
                    zoomRenderScale,
                    featureRenderScale,
                    hovered,
                    fullscreenMap);
        } else if (feature instanceof MapArea area) {
            renderMapArea(
                    guiGraphics,
                    area,
                    attributes,
                    mapCenter,
                    screenCenter,
                    rotationVector,
                    zoomLevel,
                    zoomRenderScale,
                    featureRenderScale,
                    hovered,
                    fullscreenMap);
        } else {
            WynntilsMod.warn(
                    "Could not render feature of type " + feature.getClass().getSimpleName() + " with ID "
                            + feature.getCategoryId() + ":" + feature.getFeatureId() + "!");
        }
    }

    public static void renderMapLocation(
            GuiGraphics guiGraphics,
            MapLocation location,
            ResolvedMapAttributes attributes,
            Vector2f mapCenter,
            Vector2f screenCenter,
            Vector2f rotationVector,
            float zoomLevel,
            float zoomRenderScale,
            float featureRenderScale,
            boolean hovered,
            boolean fullscreenMap) {
        float renderScale = hovered ? featureRenderScale * 1.05f : featureRenderScale;
        int labelHeight = (int) (FontRenderer.getInstance().getFont().lineHeight * renderScale * TEXT_SCALE);

        int yOffset = 0;

        Location featureLocation = location.getLocation();

        Vector2f renderVector = getRenderLocation(
                mapCenter,
                screenCenter,
                rotationVector,
                zoomRenderScale,
                new Vector2f(featureLocation.x(), featureLocation.z()));

        guiGraphics.pose().pushMatrix();
        // z-index for rendering
        guiGraphics.pose().translate(renderVector.x(), renderVector.y());
        guiGraphics.pose().scale(renderScale, renderScale);

        // Draw icon, if applicable
        float iconAlpha = Services.MapData.calculateVisibility(attributes.iconVisibility(), zoomLevel);
        Optional<MapIcon> icon = Services.MapData.getIcon(attributes.iconId());
        boolean drawIcon = iconAlpha > MINIMUM_RENDER_ALPHA;
        if (icon.isPresent() && drawIcon) {
            int iconWidth = icon.get().getWidth();
            int iconHeight = icon.get().getHeight();

            if (hovered) {
                // If this is hovered, show with full alpha
                iconAlpha = 1f;
            }

            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    icon.get().getIdentifier(),
                    attributes.iconColor().withAlpha(iconAlpha),
                    0 - iconWidth / 2f,
                    yOffset - iconHeight / 2f,
                    iconWidth,
                    iconHeight,
                    iconWidth,
                    iconHeight);
            yOffset += (iconHeight + labelHeight) / 2 + SPACING;
        }

        // Draw label, if applicable
        float labelAlpha = Services.MapData.calculateVisibility(attributes.labelVisibility(), zoomLevel);
        // Always draw labels for hovered icons, regardless of label visibility rules
        boolean drawLabel = labelAlpha > MINIMUM_RENDER_ALPHA || (drawIcon && hovered);
        if (!attributes.label().isEmpty() && drawLabel && fullscreenMap) {
            if (drawIcon && hovered) {
                // If this is hovered, show with full alpha
                labelAlpha = 1f;
            }

            CustomColor color = attributes.labelColor().withAlpha(labelAlpha);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(attributes.label()),
                            0,
                            yOffset,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            attributes.labelShadow(),
                            TEXT_SCALE);
            yOffset += labelHeight + SPACING;
        }

        // Draw level, if applicable
        int level = attributes.level();
        // Show level only for features that are displayed and hovered
        boolean drawLevel = hovered && (drawIcon || drawLabel);
        if (level >= 1 && drawLevel) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("[Lv. " + level + "]"),
                            0,
                            yOffset,
                            attributes.labelColor(),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            attributes.labelShadow(),
                            TEXT_SCALE);
        }

        // Draw decoration, if applicable
        MapDecoration decoration = attributes.iconDecoration();
        if (decoration.isVisible()) {
            decoration.render(guiGraphics, hovered, fullscreenMap, zoomLevel);
        }

        guiGraphics.pose().popMatrix();
    }

    private static void renderMapArea(
            GuiGraphics guiGraphics,
            MapArea area,
            ResolvedMapAttributes attributes,
            Vector2f mapCenter,
            Vector2f screenCenter,
            Vector2f rotationVector,
            float zoomLevel,
            float zoomRenderScale,
            float featureRenderScale,
            boolean hovered,
            boolean fullscreenMap) {
        List<Vector2f> worldVertices = area.getBoundingPolygon().vertices();

        // Transform area vertices to screen coordinates
        List<Vector2f> screenVertices = worldVertices.stream()
                .map(vertex -> getRenderLocation(mapCenter, screenCenter, rotationVector, zoomRenderScale, vertex))
                .toList();

        RenderUtils.drawPolygon(
                guiGraphics,
                attributes.fillColor(),
                attributes.borderColor(),
                attributes.borderWidth() + (hovered ? 1 : 0),
                screenVertices);

        BoundingPolygon boundingPolygon = BoundingPolygon.fromVertices(screenVertices);
        Vector2f centroid = boundingPolygon.centroid();

        float labelWidth =
                FontRenderer.getInstance().getFont().width(attributes.label()) * TEXT_SCALE * featureRenderScale;
        float maxLabelWidth = boundingPolygon.maxWidth() * featureRenderScale;
        float labelScaleModifier = Math.max(1f, labelWidth / maxLabelWidth);
        float textScale = TEXT_SCALE * featureRenderScale / labelScaleModifier;

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(attributes.label()),
                        centroid.x(),
                        centroid.y(),
                        0, // max width does not need to be set, as the text is scaled to fit the polygon
                        attributes.labelColor(),
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        attributes.labelShadow(),
                        textScale);

        // If hovered, draw secondary label
        if (hovered && !attributes.description().isEmpty()) {
            // The secondary label is rendered below the primary label, with the full scale
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(attributes.description()),
                            centroid.x(),
                            centroid.y() + FontRenderer.getInstance().getFont().lineHeight * textScale,
                            0f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE,
                            TEXT_SCALE);
        }
    }

    public static boolean isHovered(
            MapFeature mapFeature,
            ResolvedMapAttributes attributes,
            Vector2f mapCenter,
            Vector2f screenCenter,
            Vector2f rotationVector,
            Vector2i mousePos,
            float zoomRenderScale,
            float zoomLevel,
            float featureRenderScale) {
        if (mapFeature instanceof MapLocation location) {
            Location featureLocation = location.getLocation();
            float dX = (featureLocation.x() - mapCenter.x()) * zoomRenderScale;
            float dZ = (featureLocation.z() - mapCenter.y()) * zoomRenderScale;

            float tempdX = dX * rotationVector.x() - dZ * rotationVector.y();
            dZ = dX * rotationVector.y() + dZ * rotationVector.x();
            dX = tempdX;

            float renderX = screenCenter.x() + dX;
            float renderZ = screenCenter.y() + dZ;

            int labelHeight = (int) (FontRenderer.getInstance().getFont().lineHeight * featureRenderScale * TEXT_SCALE);

            int yOffset = 0;

            float iconAlpha = Services.MapData.calculateVisibility(attributes.iconVisibility(), zoomLevel);
            Optional<MapIcon> icon = Services.MapData.getIcon(attributes.iconId());
            boolean drawIcon = iconAlpha > MINIMUM_RENDER_ALPHA;
            if (icon.isPresent() && drawIcon) {
                int iconWidth = (int) (icon.get().getWidth() * featureRenderScale);
                int iconHeight = (int) (icon.get().getHeight() * featureRenderScale);

                if (MathUtils.isInside(
                        mousePos.x(),
                        mousePos.y(),
                        (int) renderX - iconWidth / 2,
                        (int) renderX + iconWidth / 2,
                        (int) renderZ - iconHeight / 2,
                        (int) renderZ + iconHeight / 2)) {
                    return true;
                }

                yOffset += (iconHeight + labelHeight) / 2 + SPACING;
            }

            float labelAlpha = Services.MapData.calculateVisibility(attributes.labelVisibility(), zoomLevel);
            boolean drawLabel = labelAlpha > MINIMUM_RENDER_ALPHA;
            if (!attributes.label().isEmpty() && drawLabel) {
                int labelWidth =
                        (int) (FontRenderer.getInstance().getFont().width(attributes.label()) * featureRenderScale);
                return MathUtils.isInside(
                        mousePos.x(),
                        mousePos.y(),
                        (int) renderX - labelWidth / 2,
                        (int) renderX + labelWidth / 2,
                        (int) renderZ + yOffset - labelHeight / 2,
                        (int) renderZ + yOffset + labelHeight / 2);
            }

            // Level and decoration are not considered for hovering

            return false;
        }

        if (mapFeature instanceof MapArea area) {
            // Transform area vertices to screen coordinates
            List<Vector2f> worldVertices = area.getBoundingPolygon().vertices();

            // Transform area vertices to screen coordinates
            List<Vector2f> screenVertices = worldVertices.stream()
                    .map(vertex -> getRenderLocation(mapCenter, screenCenter, rotationVector, zoomRenderScale, vertex))
                    .toList();

            return BoundingPolygon.fromVertices(screenVertices).contains(mousePos.x(), mousePos.y());
        }

        WynntilsMod.warn(
                "Could not check if feature of type " + mapFeature.getClass().getSimpleName() + " with ID "
                        + mapFeature.getCategoryId() + ":" + mapFeature.getFeatureId() + " is hovered!");
        return false;
    }

    /**
     * Get the render location of a feature on the map, from its world location.
     *
     * @param mapCenter       The center of the map in world coordinates
     * @param screenCenter    The center of the screen in screen coordinates
     * @param rotationVector  The rotation vector of the map (cosAngle, sinAngle)
     * @param zoomRenderScale The scale of the map
     * @param worldLocation   The location of the feature in world coordinates
     * @return The render location of the feature in screen coordinates
     */
    private static Vector2f getRenderLocation(
            Vector2f mapCenter,
            Vector2f screenCenter,
            Vector2f rotationVector,
            float zoomRenderScale,
            Vector2f worldLocation) {
        Vector2f distanceVector = worldLocation.sub(mapCenter, new Vector2f()).mul(zoomRenderScale);

        // Rotate the distance vector
        Vector2f rotatedDistanceVector = new Vector2f(
                distanceVector.x() * rotationVector.x() - distanceVector.y() * rotationVector.y(),
                distanceVector.x() * rotationVector.y() + distanceVector.y() * rotationVector.x());

        // Calculate the final render position
        return screenCenter.add(rotatedDistanceVector, new Vector2f());
    }
}
