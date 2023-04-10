/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.CodedString;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.type.DisplayPriority;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public class SeaskipperPoi implements Poi {
    private final String destination;
    private final PoiLocation destinationCenter;
    private final int combatLevel;
    private final int width;
    private final int height;
    private final int startX;
    private final int startZ;
    private final int endX;
    private final int endZ;
    private final boolean isPlayerAtPoi;

    private float renderedX;
    private float renderedX2;
    private float renderedY;
    private float renderedY2;

    public SeaskipperPoi(String destination, int combatLevel, int startX, int startZ, int endX, int endZ) {
        this.destination = destination;
        this.combatLevel = combatLevel;
        this.startX = startX;
        this.startZ = startZ;
        this.endX = endX;
        this.endZ = endZ;
        this.width = endX - startX;
        this.height = endZ - startZ;
        this.destinationCenter = new PoiLocation(startX + width / 2, null, startZ + height / 2);

        Vec3 playerLocation = McUtils.player().position();

        isPlayerAtPoi = playerLocation.x > startX
                && playerLocation.x < endX
                && playerLocation.z > startZ
                && playerLocation.z < endZ;
    }

    @Override
    public PoiLocation getLocation() {
        return destinationCenter;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.HIGHEST;
    }

    @Override
    public boolean hasStaticLocation() {
        return true;
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
        poseStack.pushPose();
        poseStack.translate(0, 0, 100);

        final float renderWidth = width * mapZoom;
        final float renderHeight = height * mapZoom;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderY - renderHeight / 2f;

        renderedX = actualRenderX;
        renderedY = actualRenderZ;
        renderedX2 = renderedX + renderWidth;
        renderedY2 = renderedY + renderHeight;

        CustomColor color;

        if (isPlayerAtPoi) {
            color = CommonColors.ORANGE;
        } else {
            color = CommonColors.WHITE;
        }

        BufferedRenderUtils.drawRect(
                poseStack,
                bufferSource,
                color.withAlpha(65),
                actualRenderX,
                actualRenderZ,
                0,
                renderWidth,
                renderHeight);

        BufferedRenderUtils.drawRectBorders(
                poseStack,
                bufferSource,
                color,
                actualRenderX,
                actualRenderZ,
                actualRenderX + renderWidth,
                actualRenderZ + renderHeight,
                0,
                1.5f);

        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        CodedString.fromString(destination),
                        actualRenderX,
                        actualRenderX + renderWidth,
                        actualRenderZ,
                        actualRenderZ + renderHeight,
                        0,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        poseStack.popPose();
    }

    public void renderAtWithoutBorders(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderX, float renderY, float mapZoom) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 100);

        final float renderWidth = width * mapZoom;
        final float renderHeight = height * mapZoom;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderY - renderHeight / 2f;

        CustomColor color;

        if (isPlayerAtPoi) {
            color = CommonColors.ORANGE;
        } else {
            color = CommonColors.WHITE;
        }

        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        CodedString.fromString(destination),
                        actualRenderX,
                        actualRenderX + renderWidth,
                        actualRenderZ,
                        actualRenderZ + renderHeight,
                        0,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        poseStack.popPose();
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        return (int) (width * mapZoom);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        return (int) (width * mapZoom);
    }

    @Override
    public String getName() {
        return destination;
    }

    public int getLevel() {
        return combatLevel;
    }

    public boolean isPlayerInside() {
        return isPlayerAtPoi;
    }

    public boolean isSelected(double mouseX, double mouseY) {
        return mouseX > renderedX && mouseX < renderedX2 && mouseY > renderedY && mouseY < renderedY2;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartZ() {
        return startZ;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndZ() {
        return endZ;
    }
}
