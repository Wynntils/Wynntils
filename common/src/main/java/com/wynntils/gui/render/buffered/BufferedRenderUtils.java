/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render.buffered;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.objects.CustomColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class BufferedRenderUtils {
    public static void drawLine(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            CustomColor color,
            float x1,
            float y1,
            float x2,
            float y2,
            float z,
            float width) {
        Matrix4f matrix = poseStack.last().pose();

        float halfWidth = width / 2;

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.POSITION_COLOR_TRIANGLE_STRIP);

        if (x1 == x2) {
            if (y2 < y1) {
                float tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            buffer.vertex(matrix, x1 - halfWidth, y1, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2 - halfWidth, y2, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x1 + halfWidth, y1, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2 + halfWidth, y2, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
        } else if (y1 == y2) {
            if (x2 < x1) {
                float tmp = x1;
                x1 = x2;
                x2 = tmp;
            }

            buffer.vertex(matrix, x1, y1 - halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x1, y1 + halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2, y2 - halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2, y2 + halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
        } else if ((x1 < x2 && y1 < y2) || (x2 < x1 && y2 < y1)) { // Top Left to Bottom Right line
            if (x2 < x1) {
                float tmp = x1;
                x1 = x2;
                x2 = tmp;

                tmp = y1;
                y1 = y2;
                y2 = tmp;
            }

            buffer.vertex(matrix, x1 + halfWidth, y1 - halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x1 - halfWidth, y1 + halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2 + halfWidth, y2 - halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2 - halfWidth, y2 + halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
        } else { // Top Right to Bottom Left Line
            if (x1 < x2) {
                float tmp = x1;
                x1 = x2;
                x2 = tmp;

                tmp = y1;
                y1 = y2;
                y2 = tmp;
            }

            buffer.vertex(matrix, x1 + halfWidth, y1 + halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x1 - halfWidth, y1 - halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2 + halfWidth, y2 + halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
            buffer.vertex(matrix, x2 - halfWidth, y2 - halfWidth, z)
                    .color(color.r, color.g, color.b, color.a)
                    .endVertex();
        }
    }

    public static void drawRectBorders(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            CustomColor color,
            float x1,
            float y1,
            float x2,
            float y2,
            float z,
            float lineWidth) {
        drawLine(poseStack, bufferSource, color, x1, y1, x2, y1, z, lineWidth);
        drawLine(poseStack, bufferSource, color, x2, y1, x2, y2, z, lineWidth);
        drawLine(poseStack, bufferSource, color, x2, y2, x1, y2, z, lineWidth);
        drawLine(poseStack, bufferSource, color, x1, y2, x1, y1, z, lineWidth);
    }

    public static void drawRect(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            CustomColor color,
            float x,
            float y,
            float z,
            float width,
            float height) {
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.POSITION_COLOR_TRIANGLE_STRIP);

        buffer.vertex(matrix, x, y + height, z)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();
        buffer.vertex(matrix, x + width, y + height, z)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();
        buffer.vertex(matrix, x + width, y, z)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();
        buffer.vertex(matrix, x, y, z).color(color.r, color.g, color.b, color.a).endVertex();
    }

    public static void drawColoredTexturedRect(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            ResourceLocation tex,
            CustomColor color,
            float alpha,
            float x,
            float y,
            float z,
            float width,
            float height) {

        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getPositionColorTextureQuad(tex));

        float[] colorArray = color.asFloatArray();

        buffer.vertex(matrix, x, y + height, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(0, 1)
                .endVertex();
        buffer.vertex(matrix, x + width, y + height, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(1, 1)
                .endVertex();
        buffer.vertex(matrix, x + width, y, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(1, 0)
                .endVertex();
        buffer.vertex(matrix, x, y, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(0, 0)
                .endVertex();
    }

    public static void drawTexturedRect(
            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Texture texture, float x, float y) {
        drawTexturedRect(
                poseStack,
                bufferSource,
                texture.resource(),
                x,
                y,
                texture.width(),
                texture.height(),
                texture.width(),
                texture.height());
    }

    public static void drawTexturedRect(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            ResourceLocation tex,
            float x,
            float y,
            float width,
            float height,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                poseStack,
                bufferSource,
                tex,
                x,
                y,
                0,
                width,
                height,
                0,
                0,
                (int) width,
                (int) height,
                textureWidth,
                textureHeight);
    }

    public static void drawTexturedRect(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            ResourceLocation tex,
            float x,
            float y,
            float z,
            float width,
            float height,
            int uOffset,
            int vOffset,
            int u,
            int v,
            int textureWidth,
            int textureHeight) {
        float uScale = 1f / textureWidth;
        float vScale = 1f / textureHeight;

        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getPositionTextureQuad(tex));

        buffer.vertex(matrix, x, y + height, z)
                .uv(uOffset * uScale, (vOffset + v) * vScale)
                .endVertex();
        buffer.vertex(matrix, x + width, y + height, z)
                .uv((uOffset + u) * uScale, (vOffset + v) * vScale)
                .endVertex();
        buffer.vertex(matrix, x + width, y, z)
                .uv((uOffset + u) * uScale, vOffset * vScale)
                .endVertex();
        buffer.vertex(matrix, x, y, z).uv(uOffset * uScale, vOffset * vScale).endVertex();
    }

    /**
     * drawProgressBar
     * Draws a progress bar (textureY1 and textureY2 now specify both textures with background being on top of the bar)
     *
     * @param poseStack poseStack to use
     * @param bufferSource bufferSource to use
     * @param texture   the texture to use
     * @param x1        left x on screen
     * @param y1        top y on screen
     * @param x2        right x on screen
     * @param y2        bottom right on screen
     * @param textureX1 texture left x for the part
     * @param textureY1 texture top y for the part (top of background)
     * @param textureX2 texture right x for the part
     * @param textureY2 texture bottom y for the part (bottom of bar)
     * @param progress  progress of the bar, 0.0f to 1.0f is left to right and 0.0f to -1.0f is right to left
     */
    public static void drawProgressBar(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2,
            float progress) {

        int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
        drawProgressBarBackground(
                poseStack, bufferSource, texture, x1, y1, x2, y2, textureX1, textureY1, textureX2, half);
        drawProgressBarForeground(
                poseStack,
                bufferSource,
                texture,
                x1,
                y1,
                x2,
                y2,
                textureX1,
                half,
                textureX2,
                textureY2 + (textureY2 - textureY1) % 2,
                progress);
    }

    public static void drawProgressBarForeground(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2,
            float progress) {
        if (progress == 0f) {
            return;
        }

        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getPositionTextureQuad(texture.resource()));

        float xMin = Math.min(x1, x2),
                xMax = Math.max(x1, x2),
                yMin = Math.min(y1, y2),
                yMax = Math.max(y1, y2),
                txMin = (float) Math.min(textureX1, textureX2) / texture.width(),
                txMax = (float) Math.max(textureX1, textureX2) / texture.width(),
                tyMin = (float) Math.min(textureY1, textureY2) / texture.height(),
                tyMax = (float) Math.max(textureY1, textureY2) / texture.height();

        if (progress < 1.0f && progress > -1.0f) {
            if (progress < 0.0f) {
                xMin += (1.0f + progress) * (xMax - xMin);
                txMin += (1.0f + progress) * (txMax - txMin);
            } else {
                xMax -= (1.0f - progress) * (xMax - xMin);
                txMax -= (1.0f - progress) * (txMax - txMin);
            }
        }

        buffer.vertex(matrix, xMin, yMin, 0).uv(txMin, tyMin).endVertex();
        buffer.vertex(matrix, xMin, yMax, 0).uv(txMin, tyMax).endVertex();
        buffer.vertex(matrix, xMax, yMax, 0).uv(txMax, tyMax).endVertex();
        buffer.vertex(matrix, xMax, yMin, 0).uv(txMax, tyMin).endVertex();
    }

    public static void drawProgressBarBackground(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2) {
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getPositionTextureQuad(texture.resource()));

        float xMin = Math.min(x1, x2),
                xMax = Math.max(x1, x2),
                yMin = Math.min(y1, y2),
                yMax = Math.max(y1, y2),
                txMin = (float) Math.min(textureX1, textureX2) / texture.width(),
                txMax = (float) Math.max(textureX1, textureX2) / texture.width(),
                tyMin = (float) Math.min(textureY1, textureY2) / texture.height(),
                tyMax = (float) Math.max(textureY1, textureY2) / texture.height();

        buffer.vertex(matrix, xMin, yMin, 0).uv(txMin, tyMin).endVertex();
        buffer.vertex(matrix, xMin, yMax, 0).uv(txMin, tyMax).endVertex();
        buffer.vertex(matrix, xMax, yMax, 0).uv(txMax, tyMax).endVertex();
        buffer.vertex(matrix, xMax, yMin, 0).uv(txMax, tyMin).endVertex();
    }
}
