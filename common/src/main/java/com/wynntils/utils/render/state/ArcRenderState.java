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
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;

public record ArcRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x,
        float y,
        float fill,
        int innerRadius,
        int outerRadius,
        float angleOffset,
        CustomColor color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    // number of possible segments for arc drawing
    private static final float MAX_STEPS = 16f;

    public ArcRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x,
            float y,
            float fill,
            int innerRadius,
            int outerRadius,
            float angleOffset,
            CustomColor color,
            ScreenRectangle scissorArea) {
        this(
                pipeline,
                textureSetup,
                pose,
                x,
                y,
                fill,
                innerRadius,
                outerRadius,
                angleOffset,
                color,
                scissorArea,
                computeBounds(x, y, outerRadius * 2, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        int segments = (int) Math.min(fill * MAX_STEPS, MAX_STEPS - 1);

        float midX = x + outerRadius;
        float midY = y + outerRadius;

        float prevOuterX = 0, prevOuterY = 0;
        float prevInnerX = 0, prevInnerY = 0;

        float angle;
        float sinAngle;
        float cosAngle;
        for (int i = 0; i <= segments; i++) {
            angle = (Mth.TWO_PI * i / (MAX_STEPS - 1f)) + angleOffset;
            sinAngle = Mth.sin(angle);
            cosAngle = Mth.cos(angle);

            float outerX = midX + sinAngle * outerRadius;
            float outerY = midY - cosAngle * outerRadius;

            float innerX = midX + sinAngle * innerRadius;
            float innerY = midY - cosAngle * innerRadius;

            if (i > 0) {
                consumer.addVertexWith2DPose(pose, prevOuterX, prevOuterY)
                        .setColor(color.r(), color.g(), color.b(), color.a());
                consumer.addVertexWith2DPose(pose, prevInnerX, prevInnerY)
                        .setColor(color.r(), color.g(), color.b(), color.a());
                consumer.addVertexWith2DPose(pose, innerX, innerY).setColor(color.r(), color.g(), color.b(), color.a());
                consumer.addVertexWith2DPose(pose, outerX, outerY).setColor(color.r(), color.g(), color.b(), color.a());
            }

            prevOuterX = outerX;
            prevOuterY = outerY;
            prevInnerX = innerX;
            prevInnerY = innerY;
        }
    }

    private static ScreenRectangle computeBounds(
            float x, float y, int diameter, Matrix3x2f pose, ScreenRectangle scissor) {
        ScreenRectangle rect = new ScreenRectangle((int) x, (int) y, diameter, diameter).transformMaxBounds(pose);

        return scissor != null ? scissor.intersection(rect) : rect;
    }
}
