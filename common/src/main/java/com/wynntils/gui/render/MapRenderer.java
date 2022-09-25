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
import com.mojang.math.Quaternion;
import com.wynntils.features.user.overlays.map.PointerType;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.map.MapProfile;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class MapRenderer {
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
            boolean followPlayerRotation,
            boolean renderUsingLinear) {
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

        List<ServicePoi> servicePois = MapModel.getAllPois().stream()
                .filter(poi -> {
                    double distanceX = McUtils.player().getX() - mapCenterX;
                    double distanceZ = McUtils.player().getZ() - mapCenterZ;

                    float textureXPosition = (float) (centerX + distanceX / scale);
                    float textureZPosition = (float) (centerZ + distanceZ / scale);

                    boolean inMap = textureXPosition >= mapTopX
                            && textureXPosition <= mapBottomX
                            && textureZPosition >= mapLeftZ
                            && textureZPosition <= mapRightZ;

                    return poi instanceof ServicePoi && inMap;
                })
                .map(poi -> (ServicePoi) poi)
                .toList();

        for (ServicePoi servicePoi : servicePois) {
            renderServicePoi(
                    poseStack,
                    mapCenterX,
                    mapCenterZ,
                    centerX,
                    centerZ,
                    scale,
                    poiScale,
                    followPlayerRotation,
                    servicePoi);
        }

        // disable rotation if necessary
        if (followPlayerRotation) {
            poseStack.popPose();
        }
    }

    public static void renderServicePoi(
            PoseStack poseStack,
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float scale,
            float poiScale,
            boolean followPlayerRotation,
            ServicePoi servicePoi) {
        // TODO: This is really basic at the moment
        //       Add fading, and other configs

        double distanceX = servicePoi.getLocation().getX() - mapCenterX;
        double distanceZ = servicePoi.getLocation().getZ() - mapCenterZ;

        float textureXPosition = (float) (centerX + distanceX / scale);
        float textureZPosition = (float) (centerZ + distanceZ / scale);

        poseStack.pushPose();

        poseStack.translate(textureXPosition, textureZPosition, 100);

        if (followPlayerRotation) {
            poseStack.mulPose(new Quaternion(
                    0F,
                    0,
                    (float) StrictMath.sin(Math.toRadians(180 + McUtils.player().getYRot()) / 2),
                    (float) StrictMath.cos(
                            -Math.toRadians(180 + McUtils.player().getYRot()) / 2)));
        }

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                servicePoi.getIcon().resource(),
                0,
                0,
                0,
                servicePoi.getIcon().width() * poiScale,
                servicePoi.getIcon().height() * poiScale,
                servicePoi.getIcon().width(),
                servicePoi.getIcon().height());

        poseStack.popPose();
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
}
