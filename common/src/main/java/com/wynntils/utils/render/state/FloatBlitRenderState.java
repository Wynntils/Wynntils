/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.state;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

// The same as BlitRenderState but using floats instead of ints to keep precision
public record FloatBlitRenderState(
        GuiPipeline pipeline,
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
            GuiPipeline pipeline,
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
        Matrix3x2f pose = this.pose();

        // If the pose contains rotation or skewing, draw it as is
        if (pose.m01 != 0 || pose.m10 != 0) {
            addVertexWith2DPose(consumer, pose, this.x1(), this.y1(), this.u1(), this.v1());
            addVertexWith2DPose(consumer, pose, this.x1(), this.y2(), this.u1(), this.v2());
            addVertexWith2DPose(consumer, pose, this.x2(), this.y2(), this.u2(), this.v2());
            addVertexWith2DPose(consumer, pose, this.x2(), this.y1(), this.u2(), this.v1());
            return;
        }

        // Otherwise, snap the vertices to the GUI pixel grid to prevent slight texture distortions
        // when using an odd numbered GUI scale
        float guiScale = (float) McUtils.guiScale();

        Vector2f topLeft = pose.transformPosition(this.x1(), this.y1(), new Vector2f());
        Vector2f bottomRight = pose.transformPosition(this.x2(), this.y2(), new Vector2f());

        float left = Math.round(topLeft.x() * guiScale) / guiScale;
        float top = Math.round(topLeft.y() * guiScale) / guiScale;
        float right = Math.round(bottomRight.x() * guiScale) / guiScale;
        float bottom = Math.round(bottomRight.y() * guiScale) / guiScale;

        addVertex(consumer, left, top, this.u1(), this.v1());
        addVertex(consumer, left, bottom, this.u1(), this.v2());
        addVertex(consumer, right, bottom, this.u2(), this.v2());
        addVertex(consumer, right, top, this.u2(), this.v1());
    }

    private void addVertexWith2DPose(VertexConsumer consumer, Matrix3x2f pose, float x, float y, float u, float v) {
        consumer.addVertexWith2DPose(pose, x, y)
                .setUv(u, v)
                .setColor(
                        this.color().r(),
                        this.color().g(),
                        this.color().b(),
                        this.color().a());
    }

    private void addVertex(VertexConsumer consumer, float x, float y, float u, float v) {
        consumer.addVertex(x, y, 0f)
                .setUv(u, v)
                .setColor(
                        this.color().r(),
                        this.color().g(),
                        this.color().b(),
                        this.color().a());
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, Matrix3x2f pose, ScreenRectangle scissorArea) {
        int minX = Mth.floor(x1);
        int minY = Mth.floor(y1);
        int maxX = Mth.ceil(x2);
        int maxY = Mth.ceil(y2);
        ScreenRectangle screenRectangle =
                new ScreenRectangle(minX, minY, maxX - minX, maxY - minY).transformMaxBounds(pose);

        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }
}
