/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.wynntils.models.map.MapTexture;
import com.wynntils.models.map.pois.Poi;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.CustomRenderType;
import com.wynntils.utils.render.type.PointerType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public final class MapRenderer {
    public static Poi hovered = null;

    public static void renderMapQuad(
            MapTexture map,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale) {
        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getMapPositionTextureQuad(map.resource()));

        renderMap(map, poseStack, buffer, centerX, centerZ, textureX, textureZ, width, height, scale);
    }

    public static void renderMapQuad(
            MapTexture map,
            PoseStack poseStack,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale) {
        RenderSystem.disableBlend();

        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, map.resource());

        // clamp map rendering
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        renderMap(map, poseStack, builder, centerX, centerZ, textureX, textureZ, width, height, scale);

        BufferUploader.drawWithShader(builder.end());
    }

    private static void renderMap(
            MapTexture map,
            PoseStack poseStack,
            VertexConsumer buffer,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale) {
        float uScale = 1f / map.getTextureWidth();
        float vScale = 1f / map.getTextureHeight();

        float halfRenderedWidth = width / 2f;
        float halfRenderedHeight = height / 2f;
        float halfTextureWidth = halfRenderedWidth * scale;
        float halfTextureHeight = halfRenderedHeight * scale;

        Matrix4f matrix = poseStack.last().pose();

        buffer.vertex(matrix, (centerX - halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                .uv((textureX - halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                .endVertex();
        buffer.vertex(matrix, (centerX + halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                .uv((textureX + halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                .endVertex();
        buffer.vertex(matrix, (centerX + halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                .uv((textureX + halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                .endVertex();
        buffer.vertex(matrix, (centerX - halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                .uv((textureX - halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                .endVertex();
    }

    public static void renderCursor(
            PoseStack poseStack,
            float renderX,
            float renderY,
            float pointerScale,
            CustomColor pointerColor,
            PointerType pointerType,
            boolean followPlayerRotation) {
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
