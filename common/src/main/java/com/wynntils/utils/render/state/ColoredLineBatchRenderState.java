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

// Batches many colored line segments (e.g. a chunk grid, polygon borders, route paths) into a single
// GuiElementRenderState submission, instead of one submission per segment.
public record ColoredLineBatchRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        List<Segment> segments,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds)
        implements GuiElementRenderState {
    public record Segment(float x1, float y1, float x2, float y2, float width, CustomColor color) {}

    public ColoredLineBatchRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            List<Segment> segments,
            ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, segments, scissorArea, computeBounds(segments, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (Segment segment : segments) {
            float dirX = segment.x2() - segment.x1();
            float dirY = segment.y2() - segment.y1();
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

            if (length == 0) continue;

            float halfWidth = segment.width() / 2f;

            float perpX = -(dirY / length) * halfWidth;
            float perpY = (dirX / length) * halfWidth;

            CustomColor color = segment.color();

            consumer.addVertexWith2DPose(pose, segment.x1() - perpX, segment.y1() - perpY)
                    .setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, segment.x1() + perpX, segment.y1() + perpY)
                    .setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, segment.x2() + perpX, segment.y2() + perpY)
                    .setColor(color.r(), color.g(), color.b(), color.a());
            consumer.addVertexWith2DPose(pose, segment.x2() - perpX, segment.y2() - perpY)
                    .setColor(color.r(), color.g(), color.b(), color.a());
        }
    }

    private static ScreenRectangle computeBounds(List<Segment> segments, Matrix3x2f pose, ScreenRectangle scissorArea) {
        if (segments.isEmpty()) return scissorArea != null ? scissorArea : new ScreenRectangle(0, 0, 0, 0);

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (Segment segment : segments) {
            float halfWidth = segment.width() / 2f;
            minX = Math.min(minX, Math.min(segment.x1(), segment.x2()) - halfWidth);
            minY = Math.min(minY, Math.min(segment.y1(), segment.y2()) - halfWidth);
            maxX = Math.max(maxX, Math.max(segment.x1(), segment.x2()) + halfWidth);
            maxY = Math.max(maxY, Math.max(segment.y1(), segment.y2()) + halfWidth);
        }

        ScreenRectangle bounds = new ScreenRectangle(
                        (int) minX, (int) minY, (int) Math.ceil(maxX - minX), (int) Math.ceil(maxY - minY))
                .transformMaxBounds(pose);

        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
