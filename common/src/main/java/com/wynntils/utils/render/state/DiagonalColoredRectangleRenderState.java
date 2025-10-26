/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record DiagonalColoredRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x1,
        float y1,
        float x2,
        float y2,
        float width,
        CustomColor color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public DiagonalColoredRectangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x0,
            float y0,
            float x1,
            float y1,
            float width,
            CustomColor color,
            ScreenRectangle scissorArea) {
        this(
                pipeline,
                textureSetup,
                pose,
                x0,
                y0,
                x1,
                y1,
                width,
                color,
                scissorArea,
                getBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len == 0) return; // avoid divide by zero

        float half = width / 2f;

        // perpendicular normalized vector
        float nx = -(dy / len) * half;
        float ny = (dx / len) * half;

        // 4 corners
        consumer.addVertexWith2DPose(pose, x1 - nx, y1 - ny).setColor(color.asInt());
        consumer.addVertexWith2DPose(pose, x1 + nx, y1 + ny).setColor(color.asInt());
        consumer.addVertexWith2DPose(pose, x2 - nx, y2 - ny).setColor(color.asInt());
        consumer.addVertexWith2DPose(pose, x2 + nx, y2 + ny).setColor(color.asInt());
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle rect = new ScreenRectangle(
                        (int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.abs(x2 - x1), (int)
                                Math.abs(y2 - y1))
                .transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(rect) : rect;
    }
}
