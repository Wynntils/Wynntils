/*
 * Copyright © Wynntils 2025-2026.
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
import org.joml.Vector2f;

// A textured, colored quad clipped to an arbitrary circular/elliptical mask (in absolute screen
// space, independent of this quad's own bounds - unlike CircleMaskRenderState, which clips a shape
// to its own bounds). Used to draw icons on a round minimap so they clip smoothly against the
// minimap's boundary instead of being shown/hidden as a whole based on their center point.
//
// Each vertex's circle-local coordinate is computed here in Java (by transforming its local
// position through `pose` into absolute space, then normalizing by the mask's center/radius) and
// packed into the UV1 vertex attribute as scaled integers - see
// assets/wynntils/shaders/core/circle_clip_tex_color.vsh for why.
public record CircleClippedBlitRenderState(
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
        float maskCenterX,
        float maskCenterY,
        float maskRadiusX,
        float maskRadiusY,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    // Matches the same constant in circle_clip_tex_color.vsh.
    private static final float CIRCLE_COORD_SCALE = 8000f;

    public CircleClippedBlitRenderState(
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
            float maskCenterX,
            float maskCenterY,
            float maskRadiusX,
            float maskRadiusY,
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
                maskCenterX,
                maskCenterY,
                maskRadiusX,
                maskRadiusY,
                scissorArea,
                getBounds(x1, y1, x2, y2, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        addVertex(consumer, x1, y1, u1, v1);
        addVertex(consumer, x1, y2, u1, v2);
        addVertex(consumer, x2, y2, u2, v2);
        addVertex(consumer, x2, y1, u2, v1);
    }

    private void addVertex(VertexConsumer consumer, float x, float y, float u, float v) {
        Vector2f absolute = pose.transformPosition(new Vector2f(x, y));
        int circleU = Math.round((absolute.x() - maskCenterX) / maskRadiusX * CIRCLE_COORD_SCALE);
        int circleV = Math.round((absolute.y() - maskCenterY) / maskRadiusY * CIRCLE_COORD_SCALE);

        consumer.addVertexWith2DPose(pose, x, y)
                .setUv(u, v)
                .setColor(color.r(), color.g(), color.b(), color.a())
                .setUv1(circleU, circleV);
    }

    private static ScreenRectangle getBounds(
            float x1, float y1, float x2, float y2, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle =
                new ScreenRectangle((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1)).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }
}
