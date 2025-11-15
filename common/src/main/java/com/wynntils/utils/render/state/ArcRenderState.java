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
    private static final float MAX_CIRCLE_STEPS = 16f;

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
                getBounds(x, y, outerRadius, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        // keeps arc from overlapping itself
        int segments = (int) Math.min(fill * MAX_CIRCLE_STEPS, MAX_CIRCLE_STEPS - 1);
        float midX = x + outerRadius;
        float midY = y + outerRadius;

        float angle;
        float sinAngle;
        float cosAngle;
        for (int i = 0; i <= segments; i++) {
            angle = Mth.TWO_PI * i / (MAX_CIRCLE_STEPS - 1f) + angleOffset;
            sinAngle = Mth.sin(angle);
            cosAngle = Mth.cos(angle);

            consumer.addVertexWith2DPose(pose, midX + sinAngle * outerRadius, midY - cosAngle * outerRadius)
                    .setColor(color.asInt());
            consumer.addVertexWith2DPose(pose, midX + sinAngle * innerRadius, midY - cosAngle * innerRadius)
                    .setColor(color.asInt());
        }
    }

    private static ScreenRectangle getBounds(
            float x, float y, int outerRadius, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle rect = new ScreenRectangle(
                        (int) Math.min(x, x + (outerRadius * 2)),
                        (int) Math.min(y, y + (outerRadius * 2)),
                        (int) Math.abs(x + (outerRadius * 2) - x),
                        (int) Math.abs(y + (outerRadius * 2) - y))
                .transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(rect) : rect;
    }
}
