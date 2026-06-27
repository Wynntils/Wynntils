/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.Vertex;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record TexturedPolygonRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        List<Vertex> vertices,
        CustomColor color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public TexturedPolygonRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            List<Vertex> vertices,
            CustomColor color,
            ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, vertices, color, scissorArea, computeBounds(vertices, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        if (vertices.size() < 3) return;

        Vertex origin = vertices.getFirst();
        for (int i = 1; i + 1 < vertices.size(); i++) {
            writeVertex(consumer, origin);
            writeVertex(consumer, vertices.get(i));
            writeVertex(consumer, vertices.get(i + 1));
            writeVertex(consumer, origin);
        }
    }

    private void writeVertex(VertexConsumer consumer, Vertex vertex) {
        consumer.addVertexWith2DPose(
                        pose, vertex.position().x(), vertex.position().y())
                .setUv(vertex.uv().x(), vertex.uv().y())
                .setColor(color.r(), color.g(), color.b(), color.a());
    }

    private static ScreenRectangle computeBounds(List<Vertex> vertices, Matrix3x2f pose, ScreenRectangle scissorArea) {
        if (vertices.isEmpty()) return scissorArea != null ? scissorArea : new ScreenRectangle(0, 0, 0, 0);

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (Vertex vertex : vertices) {
            minX = Math.min(minX, vertex.position().x());
            minY = Math.min(minY, vertex.position().y());
            maxX = Math.max(maxX, vertex.position().x());
            maxY = Math.max(maxY, vertex.position().y());
        }

        ScreenRectangle bounds = new ScreenRectangle(
                        (int) minX, (int) minY, (int) Math.ceil(maxX - minX), (int) Math.ceil(maxY - minY))
                .transformMaxBounds(pose);

        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
