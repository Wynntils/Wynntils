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

// Batches many filled triangles (e.g. polygon fills for map areas/territories) into a single
// GuiElementRenderState submission, instead of one submission per triangle (or, far worse, per pixel row).
// Vertex order within the list is preserved into the draw stream,
// so relative paint order between triangles submitted here is preserved (earlier triangles draw first).
public record ColoredTriangleBatchRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        List<Triangle> triangles,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public record Triangle(Vector2f v0, Vector2f v1, Vector2f v2, CustomColor color) {}

    public ColoredTriangleBatchRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            List<Triangle> triangles,
            ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, triangles, scissorArea, computeBounds(triangles, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (Triangle triangle : triangles) {
            CustomColor color = triangle.color();

            consumer.addVertexWith2DPose(pose, triangle.v0().x(), triangle.v0().y())
                    .setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, triangle.v1().x(), triangle.v1().y())
                    .setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, triangle.v2().x(), triangle.v2().y())
                    .setColor(color.r(), color.g(), color.b(), color.a());
            // needed as we are rendering as a quad
            consumer.addVertexWith2DPose(pose, triangle.v0().x(), triangle.v0().y())
                    .setColor(color.r(), color.g(), color.b(), color.a());
        }
    }

    private static ScreenRectangle computeBounds(
            List<Triangle> triangles, Matrix3x2f pose, ScreenRectangle scissorArea) {
        if (triangles.isEmpty()) return scissorArea != null ? scissorArea : new ScreenRectangle(0, 0, 0, 0);

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (Triangle triangle : triangles) {
            for (Vector2f vertex : List.of(triangle.v0(), triangle.v1(), triangle.v2())) {
                minX = Math.min(minX, vertex.x());
                minY = Math.min(minY, vertex.y());
                maxX = Math.max(maxX, vertex.x());
                maxY = Math.max(maxY, vertex.y());
            }
        }

        ScreenRectangle bounds = new ScreenRectangle(
                        (int) minX, (int) minY, (int) Math.ceil(maxX - minX), (int) Math.ceil(maxY - minY))
                .transformMaxBounds(pose);

        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
