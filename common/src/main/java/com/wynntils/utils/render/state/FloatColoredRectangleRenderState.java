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

// The same as ColoredRectangleRenderState but using floats instead of ints to keep precision
public record FloatColoredRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x1,
        float y1,
        float x2,
        float y2,
        CustomColor color1,
        CustomColor color2,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public FloatColoredRectangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x1,
            float y1,
            float x2,
            float y2,
            CustomColor color1,
            CustomColor color2,
            ScreenRectangle scissorArea) {
        this(
                pipeline,
                textureSetup,
                pose,
                x1,
                y1,
                x2,
                y2,
                color1,
                color2,
                scissorArea,
                getBounds(x1, y1, x2, y2, pose, scissorArea));
    }

    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1())
                .setColor(color1().r(), color1().g(), color1().b(), color1().a());
        consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y2())
                .setColor(color2().r(), color2().g(), color2().b(), color2().a());
        consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y2())
                .setColor(color2().r(), color2().g(), color2().b(), color2().a());
        consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y1())
                .setColor(color1().r(), color1().g(), color1().b(), color1().a());
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle =
                (new ScreenRectangle((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1))).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }
}
