/*
 * Copyright © Wynntils 2025-2026.
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
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public record ColoredTrianglesRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        List<Vector2f> vertices,
        CustomColor color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public ColoredTrianglesRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            List<Vector2f> vertices,
            CustomColor color,
            ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, vertices, color, scissorArea, computeBounds(vertices, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (int i = 0; i + 2 < vertices.size(); i += 3) {
            Vector2f v0 = vertices.get(i);
            Vector2f v1 = vertices.get(i + 1);
            Vector2f v2 = vertices.get(i + 2);

            consumer.addVertexWith2DPose(pose, v0.x(), v0.y()).setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, v1.x(), v1.y()).setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, v2.x(), v2.y()).setColor(color.r(), color.g(), color.b(), color.a());
            // needed as we are rendering as a quad
            consumer.addVertexWith2DPose(pose, v0.x(), v0.y()).setColor(color.r(), color.g(), color.b(), color.a());
        }
    }

    private static ScreenRectangle computeBounds(
            List<Vector2f> vertices, Matrix3x2f pose, ScreenRectangle scissorArea) {
        if (vertices.isEmpty()) return scissorArea != null ? scissorArea : new ScreenRectangle(0, 0, 0, 0);

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (Vector2f vertex : vertices) {
            minX = Math.min(minX, vertex.x());
            minY = Math.min(minY, vertex.y());
            maxX = Math.max(maxX, vertex.x());
            maxY = Math.max(maxY, vertex.y());
        }

        ScreenRectangle bounds = new ScreenRectangle(
                        (int) minX, (int) minY, (int) Math.ceil(maxX - minX), (int) Math.ceil(maxY - minY))
                .transformMaxBounds(pose);

        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
