/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record CustomRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x1,
        float y1,
        float x2,
        float y2,
        CustomColor color1,
        CustomColor color2,
        RenderDirection renderDirection,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public CustomRectangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x1,
            float y1,
            float x2,
            float y2,
            CustomColor color1,
            CustomColor color2,
            RenderDirection renderDirection,
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
                renderDirection,
                scissorArea,
                getBounds(x1, y1, x2, y2, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        if (this.renderDirection == RenderDirection.HORIZONTAL) {
            consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1())
                    .setColor(
                            this.color1().r(),
                            this.color1().g(),
                            this.color1().b(),
                            this.color1().a());
            consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y2())
                    .setColor(
                            this.color1().r(),
                            this.color1().g(),
                            this.color1().b(),
                            this.color1().a());
            consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y2())
                    .setColor(
                            this.color2().r(),
                            this.color2().g(),
                            this.color2().b(),
                            this.color2().a());
            consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y1())
                    .setColor(
                            this.color2().r(),
                            this.color2().g(),
                            this.color2().b(),
                            this.color2().a());
        } else {
            consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1())
                    .setColor(
                            this.color1().r(),
                            this.color1().g(),
                            this.color1().b(),
                            this.color1().a());
            consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y2())
                    .setColor(
                            this.color2().r(),
                            this.color2().g(),
                            this.color2().b(),
                            this.color2().a());
            consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y2())
                    .setColor(
                            this.color2().r(),
                            this.color2().g(),
                            this.color2().b(),
                            this.color2().a());
            consumer.addVertexWith2DPose(this.pose(), this.x2(), this.y1())
                    .setColor(
                            this.color1().r(),
                            this.color1().g(),
                            this.color1().b(),
                            this.color1().a());
        }
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle =
                new ScreenRectangle((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1)).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }
}
