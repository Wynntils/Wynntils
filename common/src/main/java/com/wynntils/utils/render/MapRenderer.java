/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.wynntils.models.lootruns.LootrunInstance;
import com.wynntils.models.map.MapTexture;
import com.wynntils.models.map.pois.Poi;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.CustomRenderType;
import com.wynntils.utils.render.type.PointerType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public final class MapRenderer {
    public static Poi hovered = null;

    public static void renderMapQuad(
            MapTexture map,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale) {
        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getMapPositionTextureQuad(map.resource()));

        renderMap(map, poseStack, buffer, centerX, centerZ, textureX, textureZ, width, height, scale);
    }

    public static void renderMapQuad(
            MapTexture map,
            PoseStack poseStack,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale) {
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, map.resource());

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        // clamp map rendering
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        renderMap(map, poseStack, builder, centerX, centerZ, textureX, textureZ, width, height, scale);

        BufferUploader.drawWithShader(builder.end());
    }

    private static void renderMap(
            MapTexture map,
            PoseStack poseStack,
            VertexConsumer buffer,
            float centerX,
            float centerZ,
            float textureX,
            float textureZ,
            float width,
            float height,
            float scale) {
        float uScale = 1f / map.getTextureWidth();
        float vScale = 1f / map.getTextureHeight();

        float halfRenderedWidth = width / 2f;
        float halfRenderedHeight = height / 2f;
        float halfTextureWidth = halfRenderedWidth * scale;
        float halfTextureHeight = halfRenderedHeight * scale;

        Matrix4f matrix = poseStack.last().pose();

        buffer.vertex(matrix, (centerX - halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                .uv((textureX - halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                .endVertex();
        buffer.vertex(matrix, (centerX + halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                .uv((textureX + halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                .endVertex();
        buffer.vertex(matrix, (centerX + halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                .uv((textureX + halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                .endVertex();
        buffer.vertex(matrix, (centerX - halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                .uv((textureX - halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                .endVertex();
    }

    public static void renderCursor(
            PoseStack poseStack,
            float renderX,
            float renderY,
            float pointerScale,
            CustomColor pointerColor,
            PointerType pointerType,
            boolean followPlayerRotation) {
        if (!followPlayerRotation) {
            poseStack.pushPose();
            RenderUtils.rotatePose(
                    poseStack, renderX, renderY, 180 + McUtils.player().getYRot());
        }

        float renderedWidth = pointerType.width * pointerScale;
        float renderedHeight = pointerType.height * pointerScale;

        RenderUtils.drawTexturedRectWithColor(
                poseStack,
                Texture.MAP_POINTERS.resource(),
                pointerColor,
                renderX - renderedWidth / 2f,
                renderY - renderedHeight / 2f,
                0,
                renderedWidth,
                renderedHeight,
                0,
                pointerType.textureY,
                pointerType.width,
                pointerType.height,
                Texture.MAP_POINTERS.width(),
                Texture.MAP_POINTERS.height());

        if (!followPlayerRotation) {
            poseStack.popPose();
        }
    }

    public static void renderLootrunLine(
            LootrunInstance lootrun,
            float lootrunWidth,
            PoseStack poseStack,
            float centerX,
            float centerZ,
            float mapTextureX,
            float mapTextureZ,
            float currentZoom,
            int color) {
        if (lootrun.simplifiedPath().size() < 3) return;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableCull();

        List<Vector3f> points = new ArrayList<>();

        List<Vector3f> middlePoints = new ArrayList<>();

        for (Vector3f point : lootrun.simplifiedPath()) {
            points.add(new Vector3f(
                    getRenderX((int) point.x(), mapTextureX, centerX, currentZoom),
                    getRenderZ((int) point.z(), mapTextureZ, centerZ, currentZoom),
                    0));
        }

        Vector3f last = null;
        List<Vector3f> filteredPoints = new ArrayList<>();
        for (Vector3f point : points) {
            if (last == null) {
                last = point;
                filteredPoints.add(point);
                continue;
            }

            if (new Vector3f(last).sub(point).length() > 2) {
                last = point;
                filteredPoints.add(point);
            }
        }

        points = filteredPoints;

        for (int i = 0; i < points.size() - 1; i++) {
            if (i == 0) {
                middlePoints.add(points.get(0));
            } else if (i == points.size() - 2) {
                middlePoints.add(points.get(points.size() - 1));
            } else {
                middlePoints.add(
                        new Vector3f(points.get(i)).add(points.get(i + 1)).mul(0.5f));
            }
        }

        for (int i = 1; i < middlePoints.size(); i++) {
            drawTriangles(
                    bufferBuilder,
                    poseStack,
                    middlePoints.get(i - 1),
                    points.get(i),
                    middlePoints.get(i),
                    color,
                    lootrunWidth);
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableCull();
    }

    private static void drawTriangles(
            BufferBuilder bufferBuilder,
            PoseStack poseStack,
            Vector3f p0,
            Vector3f p1,
            Vector3f p2,
            int color,
            float lineWidth) {
        Vector3f t0 = new Vector3f();
        Vector3f t2 = new Vector3f();

        p1.sub(p0, t0);
        p2.sub(p1, t2);
        t0 = new Vector3f(-t0.y(), t0.x(), 0);
        t2 = new Vector3f(-t2.y(), t2.x(), 0);

        if (signedArea(p0, p1, p2) > 0) {
            t0.mul(-1);
            t2.mul(-1);
        }

        t0.normalize();
        t2.normalize();
        t0.mul(lineWidth);
        t2.mul(lineWidth);

        Vector3f lineIntersection = lineIntersection(
                new Vector3f(p0).add(t0), new Vector3f(p1).add(t0), new Vector3f(p2).add(t2), new Vector3f(p1).add(t2));

        Vector3f anchor = new Vector3f();
        float anchorLength = Float.MAX_VALUE;

        if (lineIntersection != null) {
            lineIntersection.sub(p1, anchor);
            anchorLength = lineIntersection.length();
        }

        Vector3f p0p1 = new Vector3f(p0).sub(p1);
        Vector3f p1p2 = new Vector3f(p1).sub(p2);

        if (anchorLength > p0p1.length() || anchorLength > p1p2.length()) {
            addVertex(bufferBuilder, new Vector3f(p0).add(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p0).sub(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).add(t0), color, poseStack);

            addVertex(bufferBuilder, new Vector3f(p0).sub(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).add(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(t0), color, poseStack);

            drawRoundJoint(p1, new Vector3f(p1).add(t0), new Vector3f(p1).add(t2), p2, bufferBuilder, color, poseStack);

            addVertex(bufferBuilder, new Vector3f(p2).add(t2), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(t2), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).add(t2), color, poseStack);

            addVertex(bufferBuilder, new Vector3f(p2).add(t2), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(t2), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p2).sub(t2), color, poseStack);
        } else {
            addVertex(bufferBuilder, new Vector3f(p0).add(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p0).sub(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(anchor), color, poseStack);

            addVertex(bufferBuilder, new Vector3f(p0).add(t0), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(anchor), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).add(t0), color, poseStack);

            Vector3f _p0 = new Vector3f(p1).add(t0);
            Vector3f _p1 = new Vector3f(p1).add(t2);
            Vector3f _p2 = new Vector3f(p1).sub(anchor);

            addVertex(bufferBuilder, _p0, color, poseStack);
            addVertex(bufferBuilder, p1, color, poseStack);
            addVertex(bufferBuilder, _p2, color, poseStack);

            drawRoundJoint(p1, _p0, _p1, _p2, bufferBuilder, color, poseStack);

            addVertex(bufferBuilder, p1, color, poseStack);
            addVertex(bufferBuilder, _p1, color, poseStack);
            addVertex(bufferBuilder, _p2, color, poseStack);

            addVertex(bufferBuilder, new Vector3f(p2).add(t2), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(anchor), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).add(t2), color, poseStack);

            addVertex(bufferBuilder, new Vector3f(p2).add(t2), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p1).sub(anchor), color, poseStack);
            addVertex(bufferBuilder, new Vector3f(p2).sub(t2), color, poseStack);
        }
    }

    private static void drawRoundJoint(
            Vector3f center,
            Vector3f p0,
            Vector3f p1,
            Vector3f nextPointInLine,
            BufferBuilder bufferBuilder,
            int color,
            PoseStack poseStack) {
        float radius = new Vector3f(center).sub(p0).length();

        float angle0 = (float) Math.atan2((p1.y() - center.y()), (p1.x() - center.x()));
        float angle1 = (float) Math.atan2((p0.y() - center.y()), (p0.x() - center.x()));
        float orgAngle0 = angle0;

        if (angle1 > angle0) {
            while (angle1 - angle0 >= Math.PI - 0.001f) {
                angle1 = (float) (angle1 - 2 * Math.PI);
            }
        } else {
            while (angle0 - angle1 >= Math.PI - 0.001f) {
                angle0 = (float) (angle0 - 2 * Math.PI);
            }
        }

        float angleDiff = angle1 - angle0;

        if (Math.abs(angleDiff) >= Math.PI - 0.001f && Math.abs(angleDiff) <= Math.PI + 0.001f) {
            Vector3f r1 = new Vector3f(center).sub(nextPointInLine);

            if (r1.x() == 0) {
                if (r1.y() > 0) {
                    angleDiff = -angleDiff;
                }
            } else if (r1.x() >= -0.001f) {
                angleDiff = -angleDiff;
            }
        }

        int nSegments = (int) Math.abs(angleDiff * radius / 2);
        nSegments++;

        float angleInc = angleDiff / nSegments;

        for (int i = 0; i < nSegments; i++) {
            addVertex(bufferBuilder, center, color, poseStack);
            addVertex(
                    bufferBuilder,
                    new Vector3f(
                            (float) (center.x() + radius * Math.cos(orgAngle0 + angleInc * i)),
                            (float) (center.y() + radius * Math.sin(orgAngle0 + angleInc * i)),
                            0),
                    color,
                    poseStack);
            addVertex(
                    bufferBuilder,
                    new Vector3f(
                            (float) (center.x() + radius * Math.cos(orgAngle0 + angleInc * (1 + i))),
                            (float) (center.y() + radius * Math.sin(orgAngle0 + angleInc * (1 + i))),
                            0),
                    color,
                    poseStack);
        }
    }

    private static void addVertex(BufferBuilder bufferBuilder, Vector3f pos, int color, PoseStack poseStack) {
        bufferBuilder
                .vertex(poseStack.last().pose(), pos.x(), pos.y(), pos.z())
                .color(color)
                .endVertex();
    }

    private static float signedArea(Vector3f p0, Vector3f p1, Vector3f p2) {
        return (p1.x() - p0.x()) * (p2.y() - p0.y()) - (p2.x() - p0.x()) * (p1.y() - p0.y());
    }

    private static Vector3f lineIntersection(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3) {
        float epsilon = 0.001f;

        float a0 = p1.y() - p0.y();
        float b0 = p0.x() - p1.x();

        float a1 = p3.y() - p2.y();
        float b1 = p2.x() - p3.x();

        var det = a0 * b1 - a1 * b0;
        if (det > -epsilon && det < epsilon) {
            return null;
        } else {
            var c0 = a0 * p0.x + b0 * p0.y;
            var c1 = a1 * p2.x + b1 * p2.y;

            var x = (b1 * c0 - b0 * c1) / det;
            var y = (a0 * c1 - a1 * c0) / det;
            return new Vector3f(x, y, 0);
        }
    }

    /**
     * {@param poi} POI that we get the render coordinate for
     * {@param mapCenterX} center coordinates of map (in-game coordinates)
     * {@param centerX} center coordinates of map (screen render coordinates)
     * {@param currentZoom} the bigger, the more detailed the map is
     */
    public static float getRenderX(Poi poi, float mapCenterX, float centerX, float currentZoom) {
        double distanceX = poi.getLocation().getX() - mapCenterX;
        return (float) (centerX + distanceX * currentZoom);
    }

    public static float getRenderX(int worldX, float mapCenterX, float centerX, float currentZoom) {
        double distanceX = worldX - mapCenterX;
        return (float) (centerX + distanceX * currentZoom);
    }

    /**
     * {@param poi} POI that we get the render coordinate for
     * {@param mapCenterZ} center coordinates of map (in-game coordinates)
     * {@param centerZ} center coordinates of map (screen render coordinates)
     * {@param currentZoom} the bigger, the more detailed the map is
     */
    public static float getRenderZ(Poi poi, float mapCenterZ, float centerZ, float currentZoom) {
        double distanceZ = poi.getLocation().getZ() - mapCenterZ;
        return (float) (centerZ + distanceZ * currentZoom);
    }

    public static float getRenderZ(int worldZ, float mapCenterZ, float centerZ, float currentZoom) {
        double distanceZ = worldZ - mapCenterZ;
        return (float) (centerZ + distanceZ * currentZoom);
    }
}
