/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;

public record MulticoloredRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x1,
        float y1,
        float x2,
        float y2,
        float width,
        List<CustomColor> colors,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public MulticoloredRectangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x0,
            float y0,
            float x1,
            float y1,
            float width,
            List<CustomColor> colors,
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
                colors,
                scissorArea,
                getBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float splitX = width / (colors.size() - 1);

        for (int i = 0; i < colors.size(); i++) {
            CustomColor color = colors.get(i);
            float leftX = Mth.clamp(x1 + splitX * (i - 1), x1, x2);
            float centerX = Mth.clamp(x1 + splitX * i, x1, x2);
            float rightX = Mth.clamp(x1 + splitX * (i + 1), x1, x2);

            // bottom left
            consumer.addVertexWith2DPose(pose, leftX, y2).setColor(color.r(), color.g(), color.b(), color.a());
            // bottom right
            consumer.addVertexWith2DPose(pose, centerX, y2).setColor(color.r(), color.g(), color.b(), color.a());
            // top right
            consumer.addVertexWith2DPose(pose, rightX, y1).setColor(color.r(), color.g(), color.b(), color.a());
            // top left
            consumer.addVertexWith2DPose(pose, centerX, y1).setColor(color.r(), color.g(), color.b(), color.a());
        }
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
