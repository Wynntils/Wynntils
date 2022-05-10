/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderUtils {

    public static final ResourceLocation highlight = new ResourceLocation("wynntils", "textures/highlight.png");

    public static void drawRect(int color, int x, int y, int width, int height) {
        drawRect(new PoseStack(), color, x, y, 0, width, height);
    }

    public static void drawRect(PoseStack poseStack, int color, int x, int y, int z, int width, int height) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder
                .vertex(matrix, x, y + height, z)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x + width, y + height, z)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x + width, y, z)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
    }

    public static void drawTexturedRect(
            ResourceLocation tex, int x, int y, int width, int height, int textureWidth, int textureHeight) {
        drawTexturedRect(
                new PoseStack(), tex, x, y, 0, width, height, 0, 0, width, height, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(
            PoseStack poseStack,
            ResourceLocation tex,
            int x,
            int y,
            int z,
            int width,
            int height,
            int uOffset,
            int vOffset,
            int u,
            int v,
            int textureWidth,
            int textureHeight) {
        float uScale = 1f / textureWidth;
        float vScale = 1f / textureHeight;

        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tex);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder
                .vertex(matrix, x, y + height, z)
                .uv(uOffset * uScale, (vOffset + v) * vScale)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x + width, y + height, z)
                .uv((uOffset + u) * uScale, (vOffset + v) * vScale)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x + width, y, z)
                .uv((uOffset + u) * uScale, vOffset * vScale)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x, y, z)
                .uv(uOffset * uScale, vOffset * vScale)
                .endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

    public static void drawTexturedRectWithColor(
            ResourceLocation tex, int color, int x, int y, int width, int height, int textureWidth, int textureHeight) {
        drawTexturedRectWithColor(
                new PoseStack(), tex, color, x, y, 0, width, height, 0, 0, width, height, textureWidth, textureHeight);
    }

    public static void drawTexturedRectWithColor(
            PoseStack poseStack,
            ResourceLocation tex,
            int color,
            int x,
            int y,
            int z,
            int width,
            int height,
            int uOffset,
            int vOffset,
            int u,
            int v,
            int textureWidth,
            int textureHeight) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        float uScale = 1f / textureWidth;
        float vScale = 1f / textureHeight;

        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, tex);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder
                .vertex(matrix, x, y + height, z)
                .uv(uOffset * uScale, (vOffset + v) * vScale)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x + width, y + height, z)
                .uv((uOffset + u) * uScale, (vOffset + v) * vScale)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x + width, y, z)
                .uv((uOffset + u) * uScale, vOffset * vScale)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix, x, y, z)
                .uv(uOffset * uScale, vOffset * vScale)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
    }
}
