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
            float x1,
            float y1,
            float x2,
            float y2,
            float width,
            CustomColor color,
            ScreenRectangle scissorArea) {
        this(
                pipeline,
                textureSetup,
                pose,
                x1,
                y1,
                x2,
                y2,
                width,
                color,
                scissorArea,
                getBounds(x1, y1, x2, y2, width, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float dirX = x2 - x1;
        float dirY = y2 - y1;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (length == 0) return;

        float halfWidth = width / 2f;

        float perpX = -(dirY / length) * halfWidth;
        float perpY = (dirX / length) * halfWidth;

        float x1Left = x1 - perpX;
        float y1Left = y1 - perpY;

        float x1Right = x1 + perpX;
        float y1Right = y1 + perpY;

        float x2Right = x2 + perpX;
        float y2Right = y2 + perpY;

        float x2Left = x2 - perpX;
        float y2Left = y2 - perpY;

        consumer.addVertexWith2DPose(pose, x1Left, y1Left).setColor(color.asInt());
        consumer.addVertexWith2DPose(pose, x1Right, y1Right).setColor(color.asInt());
        consumer.addVertexWith2DPose(pose, x2Right, y2Right).setColor(color.asInt());
        consumer.addVertexWith2DPose(pose, x2Left, y2Left).setColor(color.asInt());
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, float width, Matrix3x2f pose, ScreenRectangle scissorArea) {
        float dirX = x2 - x1;
        float dirY = y2 - y1;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (length == 0) {
            length = 1;
        }

        float halfWidth = width / 2f;

        float perpX = -(dirY / length) * halfWidth;
        float perpY = (dirX / length) * halfWidth;

        float x1Left = x1 - perpX;
        float y1Left = y1 - perpY;

        float x1Right = x1 + perpX;
        float y1Right = y1 + perpY;

        float x2Right = x2 + perpX;
        float y2Right = y2 + perpY;

        float x2Left = x2 - perpX;
        float y2Left = y2 - perpY;

        float minX = Math.min(Math.min(x1Left, x1Right), Math.min(x2Left, x2Right));
        float maxX = Math.max(Math.max(x1Left, x1Right), Math.max(x2Left, x2Right));

        float minY = Math.min(Math.min(y1Left, y1Right), Math.min(y2Left, y2Right));
        float maxY = Math.max(Math.max(y1Left, y1Right), Math.max(y2Left, y2Right));

        ScreenRectangle rect = new ScreenRectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY))
                .transformMaxBounds(pose);

        return scissorArea != null ? scissorArea.intersection(rect) : rect;
    }
}
