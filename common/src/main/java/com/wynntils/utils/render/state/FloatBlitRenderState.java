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

// The same as BlitRenderState but using floats instead of ints to keep precision
public record FloatBlitRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x1,
        float y1,
        float x2,
        float y2,
        float u1,
        float u2,
        float v1,
        float v2,
        CustomColor color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public FloatBlitRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x1,
            float y1,
            float x2,
            float y2,
            float u1,
            float u2,
            float v1,
            float v2,
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
                u1,
                u2,
                v1,
                v2,
                color,
                scissorArea,
                getBounds(x1, y1, x2, y2, pose, scissorArea));
    }

    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1())
                .setUv(this.u1(), this.v1())
                .setColor(
                        this.color().r(),
                        this.color().g(),
                        this.color().b(),
                        this.color().a());
        consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y2())
                .setUv(this.u1(), this.v2())
                .setColor(
                        this.color().r(),
                        this.color().g(),
                        this.color().b(),
                        this.color().a());
        consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y2())
                .setUv(this.u2(), this.v2())
                .setColor(
                        this.color().r(),
                        this.color().g(),
                        this.color().b(),
                        this.color().a());
        consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y1())
                .setUv(this.u2(), this.v1())
                .setColor(
                        this.color().r(),
                        this.color().g(),
                        this.color().b(),
                        this.color().a());
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle =
                (new ScreenRectangle((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1))).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }
}
