/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record HorizontalRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x0,
        int y0,
        int x1,
        int y1,
        int col1,
        int col2,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public HorizontalRectangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            int x0,
            int y0,
            int x1,
            int y1,
            int col1,
            int col2,
            ScreenRectangle scissorArea) {
        this(
                pipeline,
                textureSetup,
                pose,
                x0,
                y0,
                x1,
                y1,
                col1,
                col2,
                scissorArea,
                getBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose(), (float) this.x0(), (float) this.y0())
                .setColor(this.col1());
        consumer.addVertexWith2DPose(this.pose(), (float) this.x0(), (float) this.y1())
                .setColor(this.col1());
        consumer.addVertexWith2DPose(this.pose(), (float) this.x1(), (float) this.y1())
                .setColor(this.col2());
        consumer.addVertexWith2DPose(this.pose(), (float) this.x1(), (float) this.y0())
                .setColor(this.col2());
    }

    private static ScreenRectangle getBounds(
            int x0, int y0, int x1, int y1, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }
}
