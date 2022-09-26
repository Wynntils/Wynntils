/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.wynntils.features.user.overlays.map.PointerType;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Pair;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.map.MapProfile;
import com.wynntils.wynn.model.map.poi.LabelPoi;
import com.wynntils.wynn.model.map.poi.Poi;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class MapRenderer {
    private static Poi hovered = null;

    // TODO: Support circle map rendering
    public static void renderMapQuad(
            MapProfile map,
            PoseStack poseStack,
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale,
            float poiScale,
            Pair<Integer, Integer> mouseCoordinates,
            boolean renderMapLabels,
            boolean followPlayerRotation,
            boolean renderUsingLinear) {
        RenderSystem.disableBlend();

        // enable rotation if necessary
        if (followPlayerRotation) {
            poseStack.pushPose();
            RenderUtils.rotatePose(
                    poseStack, centerX, centerZ, 180 - McUtils.player().getYRot());
        }

        // has to be before setting shader texture
        int option = renderUsingLinear ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, option);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, option);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, map.resource());

        // clamp map rendering
        // has to be after setting shader texture
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);

        float uScale = 1f / map.getTextureWidth();
        float vScale = 1f / map.getTextureHeight();

        // avoid rotational overpass - This is a rather loose oversizing, if possible later
        // use trignometry, etc. to find a better one
        float extraFactor = 1F;
        if (followPlayerRotation) {
            // 1.5 > sqrt(2);
            extraFactor = 1.5F;

            if (width > height) {
                extraFactor *= width / height;
            } else {
                extraFactor *= height / width;
            }
        }

        float halfRenderedWidth = width / 2f * extraFactor;
        float halfRenderedHeight = height / 2f * extraFactor;
        float halfTextureWidth = halfRenderedWidth * scale;
        float halfTextureHeight = halfRenderedHeight * scale;

        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder
                .vertex(matrix, (centerX - halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                .uv((textureX - halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                .endVertex();
        bufferBuilder
                .vertex(matrix, (centerX + halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                .uv((textureX + halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                .endVertex();
        bufferBuilder
                .vertex(matrix, (centerX + halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                .uv((textureX + halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                .endVertex();
        bufferBuilder
                .vertex(matrix, (centerX - halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                .uv((textureX - halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                .endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);

        float mapTopX = centerX - halfRenderedWidth;
        float mapLeftZ = centerZ - halfRenderedHeight;
        float mapBottomX = centerX + halfRenderedWidth;
        float mapRightZ = centerZ + halfRenderedHeight;

        if (renderMapLabels) {
            renderLabelPois(poseStack, mapCenterX, mapCenterZ, centerX, centerZ, scale);
        }

        // disable rotation if necessary
        if (followPlayerRotation) {
            poseStack.popPose();
        }

        renderTexturedPois(
                poseStack,
                mapCenterX,
                mapCenterZ,
                centerX,
                centerZ,
                scale,
                poiScale,
                mouseCoordinates,
                followPlayerRotation,
                mapTopX,
                mapLeftZ,
                mapBottomX,
                mapRightZ);
    }

    private static void renderLabelPois(
            PoseStack poseStack, float mapCenterX, float mapCenterZ, float centerX, float centerZ, float scale) {
        List<LabelPoi> labelPois = MapModel.getAllPois().stream()
                .filter(poi -> poi instanceof LabelPoi)
                .map(poi -> (LabelPoi) poi)
                .toList();

        for (LabelPoi labelPoi : labelPois) {
            float x = labelPoi.getLocation().getX();
            float z = labelPoi.getLocation().getZ();
            double distanceX = x - mapCenterX;
            double distanceZ = z - mapCenterZ;

            float textureXPosition = (float) (centerX
                    + distanceX / scale
                    - FontRenderer.getInstance()
                                    .getFont()
                                    .width(labelPoi.getLabel().getName())
                            / 2f
                            / scale);
            float textureZPosition = (float) (centerZ + distanceZ / scale - 4.5f / scale);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            labelPoi.getLabel().getName(),
                            textureXPosition,
                            textureZPosition,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NORMAL);
        }
    }

    private static void renderTexturedPois(
            PoseStack poseStack,
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float scale,
            float poiScale,
            Pair<Integer, Integer> mouseCoordinates,
            boolean followPlayerRotation,
            float mapTopX,
            float mapLeftZ,
            float mapBottomX,
            float mapRightZ) {
        List<Poi> pois = MapModel.getAllPois().stream()
                .filter(poi -> {
                    if (poi.getIcon() == null) return false;

                    return isPoiVisible(
                            mapCenterX,
                            mapCenterZ,
                            centerX,
                            centerZ,
                            scale,
                            mapTopX,
                            mapLeftZ,
                            mapBottomX,
                            mapRightZ,
                            poi);
                })
                .sorted(Comparator.comparing(poi -> -poi.getLocation().getY()))
                .toList();

        if (mouseCoordinates != null) {
            hovered = null;
        }

        final double rotationRadians = Math.toRadians(McUtils.player().getYRot());
        final float sinRotationRadians = (float) StrictMath.sin(rotationRadians);
        final float cosRotationRadians = (float) -StrictMath.cos(rotationRadians);

        for (Poi poi : pois) {
            Pair<Float, Float> renderPositions = getRenderPositions(
                    followPlayerRotation,
                    mapCenterX,
                    mapCenterZ,
                    centerX,
                    centerZ,
                    poiScale,
                    scale,
                    sinRotationRadians,
                    cosRotationRadians,
                    poi);
            float renderX = renderPositions.a;
            float renderZ = renderPositions.b;

            float width = poi.getIcon().width() * poiScale;
            float height = poi.getIcon().height() * poiScale;

            if (mouseCoordinates != null) {
                int mouseX = mouseCoordinates.a;
                int mouseY = mouseCoordinates.b;

                if (mouseX >= renderX && mouseX <= renderX + width && mouseY >= renderZ && mouseY <= renderZ + height) {
                    hovered = poi;
                }
            }

            renderTexturedPoi(poseStack, renderX, renderZ, width, height, hovered == poi, poi);
        }

        if (McUtils.mc().screen instanceof MainMapScreen mainMapScreen) {
            mainMapScreen.setHovered(hovered);
        }

        if (hovered != null && mouseCoordinates != null) {
            float textureXPosition = getRenderX(hovered, mapCenterX, centerX, 1f / scale);
            float textureZPosition = getRenderZ(hovered, mapCenterZ, centerZ, 1f / scale);

            float width = hovered.getIcon().width() * poiScale;
            float height = hovered.getIcon().height() * poiScale;

            poseStack.pushPose();

            float renderX = textureXPosition - width / 2f;
            float renderZ = textureZPosition - height / 2f;
            poseStack.translate(renderX, renderZ, 0);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            hovered.getName(),
                            width / 2f,
                            20,
                            CommonColors.GREEN,
                            HorizontalAlignment.Center,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.OUTLINE);

            poseStack.popPose();
        }
    }

    private static boolean isPoiVisible(
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float scale,
            float mapTopX,
            float mapLeftZ,
            float mapBottomX,
            float mapRightZ,
            Poi poi) {
        float textureXPosition = getRenderX(poi, mapCenterX, centerX, 1f / scale);
        float textureZPosition = getRenderZ(poi, mapCenterZ, centerZ, 1f / scale);

        return textureXPosition >= mapTopX
                && textureXPosition <= mapBottomX
                && textureZPosition >= mapLeftZ
                && textureZPosition <= mapRightZ;
    }

    public static void renderTexturedPoi(
            PoseStack poseStack,
            float renderX,
            float renderZ,
            float width,
            float height,
            boolean hovered,
            Poi renderablePoi) {
        // TODO: This is really basic at the moment
        //       Add fading, and other configs

        if (hovered) {
            width *= 1.05;
            height *= 1.05;
        }

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                renderablePoi.getIcon().resource(),
                renderX,
                renderZ,
                renderablePoi.getLocation().getY(),
                width,
                height,
                renderablePoi.getIcon().width(),
                renderablePoi.getIcon().height());
    }

    private static Pair<Float, Float> getRenderPositions(
            boolean followPlayerRotation,
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float poiScale,
            float scale,
            float sinRotationRadians,
            float cosRotationRadians,
            Poi servicePoi) {
        float textureXPosition;
        float textureZPosition;

        if (followPlayerRotation) {
            float dX = (servicePoi.getLocation().getX() - mapCenterX) / scale;
            float dZ = (servicePoi.getLocation().getZ() - mapCenterZ) / scale;

            textureXPosition = centerX + (dX * cosRotationRadians - dZ * sinRotationRadians);
            textureZPosition = centerZ + (dX * sinRotationRadians + dZ * cosRotationRadians);
        } else {
            textureXPosition = getRenderX(servicePoi, mapCenterX, centerX, 1f / scale);
            textureZPosition = getRenderZ(servicePoi, mapCenterZ, centerZ, 1f / scale);
        }

        float width = servicePoi.getIcon().width() * poiScale;
        float height = servicePoi.getIcon().height() * poiScale;

        float renderX = textureXPosition - width / 2f;
        float renderZ = textureZPosition - height / 2f;

        return new Pair<>(renderX, renderZ);
    }

    public static void renderCursor(
            PoseStack poseStack,
            float renderX,
            float renderY,
            float pointerScale,
            boolean followPlayerRotation,
            CustomColor pointerColor,
            PointerType pointerType) {
        if (!followPlayerRotation) {
            poseStack.pushPose();
            RenderUtils.rotatePose(
                    poseStack, renderX, renderY, 180 + McUtils.player().getYRot());
        }

        float renderedWidth = pointerType.width * pointerScale;
        float renderedHeight = pointerType.height * pointerScale;

        RenderUtils.drawTexturedRectWithColor(
                poseStack,
                Texture.MAP_POINTERS.resource(),
                pointerColor,
                renderX - renderedWidth / 2f,
                renderY - renderedHeight / 2f,
                0,
                renderedWidth,
                renderedHeight,
                0,
                pointerType.textureY,
                pointerType.width,
                pointerType.height,
                Texture.MAP_POINTERS.width(),
                Texture.MAP_POINTERS.height());

        if (!followPlayerRotation) {
            poseStack.popPose();
        }
    }

    /**
     * {@param poi} POI that we get the render coordinate for
     * {@param mapCenterX} center coordinates of map (in-game coordinates)
     * {@param centerX} center coordinates of map (screen render coordinates)
     * {@param currentZoom} the bigger, the more detailed the map is
     */
    public static float getRenderX(Poi poi, float mapCenterX, float centerX, float currentZoom) {
        double distanceX = poi.getLocation().getX() - mapCenterX;
        return (float) (centerX + distanceX * currentZoom);
    }

    /**
     * {@param poi} POI that we get the render coordinate for
     * {@param mapCenterZ} center coordinates of map (in-game coordinates)
     * {@param centerZ} center coordinates of map (screen render coordinates)
     * {@param currentZoom} the bigger, the more detailed the map is
     */
    public static float getRenderZ(Poi poi, float mapCenterZ, float centerZ, float currentZoom) {
        double distanceZ = poi.getLocation().getZ() - mapCenterZ;
        return (float) (centerZ + distanceZ * currentZoom);
    }
}
