/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.services.map.MapTexture;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.pipelines.CustomRenderPipelines;
import com.wynntils.utils.render.state.CircleMaskRenderState;
import com.wynntils.utils.render.state.ColoredLineBatchRenderState;
import com.wynntils.utils.render.state.TexturedPolygonRenderState;
import com.wynntils.utils.render.type.CircleMask;
import com.wynntils.utils.render.type.PointerType;
import com.wynntils.utils.render.type.Vertex;
import com.wynntils.utils.type.BoundingBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class MapRenderer {
    // The possible zoom levels range between [1, ZOOM_LEVELS] (inclusive).
    // Levels can be thought of as a percentage of the zoom,
    // with 1 being the minimum zoom, and ZOOM_LEVELS being the maximum zoom.
    public static final int ZOOM_LEVELS = 100;

    // This value should be the nearest step to the default zoom, 1.0f.
    public static final float DEFAULT_ZOOM_LEVEL = 60;

    // The minimum and maximum zoom values. This is the range of the zoom.
    // The minimum zoom is where the map is at its smallest, and the maximum zoom is where the map is at its largest.
    // The ratio is 20:1 when the zoom is at it's minimum, 1:10 when the zoom is at it's maximum.
    // The zoom is adjusted by GUI scale, to make sure these ratios are consistent across all GUI scales.
    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 10f;

    // These don't have significance, they are just used to calculate the zoom,
    // and are cached for performance.
    private static final double MIN_ZOOM_LOG = Math.log(MIN_ZOOM);
    private static final double MAX_ZOOM_LOG = Math.log(MAX_ZOOM);

    private static final float CHUNK_LINE_WIDTH = 1.0f;
    public static final int CIRCLE_MASK_SEGMENTS = 64;

    // Zoom is calculated using exponential interpolation between MIN_ZOOM and MAX_ZOOM.
    // The result is that the zoom increases uniformly for all levels, no matter the current zoom.
    // To achieve this, we need to exponentially increase the zoom value for each step.
    // - Taking the log of the zoom value, and then linearly interpolating between the log values.
    // - This means that the zoom values for each level become exponentially larger.
    // - Steps are 1-based (1 to ZOOM_LEVELS), so we subtract 1 from the level to get the correct zoom value,
    //   to ensure that the real zoom values are in the range [MIN_ZOOM, MAX_ZOOM] (including the boundaries).
    public static float getZoomRenderScaleFromLevel(float zoomLevel) {
        double guiScale = McUtils.guiScale();
        double logGuiScale = Math.log(guiScale);

        // log(MIN_ZOOM / guiScale) = log(MIN_ZOOM) - log(guiScale)
        double logMinZoomGuiScale = MIN_ZOOM_LOG - logGuiScale;
        // log(MAX_ZOOM / guiScale) = log(MAX_ZOOM) - log(guiScale)
        double logMaxZoomGuiScale = MAX_ZOOM_LOG - logGuiScale;

        return (float) Math.exp(
                logMinZoomGuiScale + (logMaxZoomGuiScale - logMinZoomGuiScale) * (zoomLevel - 1) / (ZOOM_LEVELS - 1));
    }

    public static void renderMapTile(
            GuiGraphics guiGraphics,
            MapTexture map,
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float zoomRenderScale,
            BoundingBox view) {
        float x1 = map.getX1();
        float z1 = map.getZ1();
        float x2 = map.getX2() + 1f;
        float z2 = map.getZ2() + 1f;

        float vx1 = Math.max(view.x1(), x1);
        float vz1 = Math.max(view.z1(), z1);
        float vx2 = Math.min(view.x2(), x2);
        float vz2 = Math.min(view.z2(), z2);

        if (vx1 >= vx2 || vz1 >= vz2) return;

        float sx1 = centerX + (vx1 - mapCenterX) * zoomRenderScale;
        float sy1 = centerZ + (vz1 - mapCenterZ) * zoomRenderScale;
        float sx2 = centerX + (vx2 - mapCenterX) * zoomRenderScale;
        float sy2 = centerZ + (vz2 - mapCenterZ) * zoomRenderScale;

        float u1 = (vx1 - x1);
        float v1 = (vz1 - z1);
        float u2 = (vx2 - x1);
        float v2 = (vz2 - z1);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                map.identifier(),
                CommonColors.WHITE,
                sx1,
                sy1,
                sx2 - sx1,
                sy2 - sy1,
                u1,
                v1,
                u2 - u1,
                v2 - v1,
                map.getTextureWidth(),
                map.getTextureHeight());
    }

    public static void renderCircularBackground(
            GuiGraphics guiGraphics, CustomColor color, float x, float y, float width, float height) {
        // The circle is rendered as a single quad; CIRCLE_MASK_PIPELINE's fragment shader masks it
        // to an exact ellipse per-pixel.
        guiGraphics.guiRenderState.submitGuiElement(new CircleMaskRenderState(
                CustomRenderPipelines.CIRCLE_MASK_PIPELINE,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                x,
                y,
                x + width,
                y + height,
                color,
                guiGraphics.scissorStack.peek()));
    }

    public static void renderCircularMapTile(
            GuiGraphics guiGraphics,
            MapTexture map,
            float mapCenterX,
            float mapCenterZ,
            float centerX,
            float centerZ,
            float zoomRenderScale,
            BoundingBox view,
            float maskX,
            float maskY,
            float maskWidth,
            float maskHeight) {
        float mapMinX = map.getX1();
        float mapMinZ = map.getZ1();
        float mapMaxX = map.getX2() + 1f;
        float mapMaxZ = map.getZ2() + 1f;

        float clippedMinX = Math.max(view.x1(), mapMinX);
        float clippedMinZ = Math.max(view.z1(), mapMinZ);
        float clippedMaxX = Math.min(view.x2(), mapMaxX);
        float clippedMaxZ = Math.min(view.z2(), mapMaxZ);

        if (clippedMinX >= clippedMaxX || clippedMinZ >= clippedMaxZ) return;

        float screenMinX = centerX + (clippedMinX - mapCenterX) * zoomRenderScale;
        float screenMinY = centerZ + (clippedMinZ - mapCenterZ) * zoomRenderScale;
        float screenMaxX = centerX + (clippedMaxX - mapCenterX) * zoomRenderScale;
        float screenMaxY = centerZ + (clippedMaxZ - mapCenterZ) * zoomRenderScale;

        float u1 = (clippedMinX - mapMinX) / map.getTextureWidth();
        float v1 = (clippedMinZ - mapMinZ) / map.getTextureHeight();
        float u2 = (clippedMaxX - mapMinX) / map.getTextureWidth();
        float v2 = (clippedMaxZ - mapMinZ) / map.getTextureHeight();

        renderCircleMaskedTexturedRect(
                guiGraphics,
                map.identifier(),
                CommonColors.WHITE,
                screenMinX,
                screenMinY,
                screenMaxX,
                screenMaxY,
                u1,
                u2,
                v1,
                v2,
                CircleMask.fromBounds(maskX, maskY, maskWidth, maskHeight));
    }

    public static void renderCursor(
            GuiGraphics guiGraphics,
            float renderX,
            float renderY,
            float pointerScale,
            CustomColor pointerColor,
            PointerType pointerType,
            boolean followPlayerRotation) {
        float rotationAngle;
        if (followPlayerRotation) {
            rotationAngle = McUtils.player().getYRot()
                    - McUtils.mc().gameRenderer.getMainCamera().yRot();
        } else {
            rotationAngle = 180 + McUtils.player().getYRot();
        }

        guiGraphics.pose().pushMatrix();
        RenderUtils.rotatePose(guiGraphics.pose(), renderX, renderY, rotationAngle);

        float renderedWidth = pointerType.width * pointerScale;
        float renderedHeight = pointerType.height * pointerScale;

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MAP_POINTERS.identifier(),
                pointerColor,
                renderX - renderedWidth / 2f,
                renderY - renderedHeight / 2f,
                renderedWidth,
                renderedHeight,
                0,
                pointerType.textureY,
                pointerType.width,
                pointerType.height,
                Texture.MAP_POINTERS.width(),
                Texture.MAP_POINTERS.height());

        guiGraphics.pose().popMatrix();
    }

    public static void renderChunks(
            GuiGraphics guiGraphics,
            BoundingBox renderedWorldBoundingBox,
            Set<Long> mappedChunks,
            float mapCenterX,
            float centerX,
            float mapCenterZ,
            float centerZ,
            float zoomRenderScale) {
        ChunkPos topLeft =
                new ChunkPos(new BlockPos((int) renderedWorldBoundingBox.x1(), 0, (int) renderedWorldBoundingBox.z1()));
        ChunkPos bottomRight =
                new ChunkPos(new BlockPos((int) renderedWorldBoundingBox.x2(), 0, (int) renderedWorldBoundingBox.z2()));

        // Accumulate every grid line into a single batch, submitted once below, instead of one
        // GuiElementRenderState submission per line (which is O(visible chunks) submissions/frame
        // and was the dominant rendering cost under the 1.21.6+ GUI render state model).
        List<ColoredLineBatchRenderState.Segment> segments = new ArrayList<>();

        // Render the chunk grid, with a 1px border around each chunk.
        for (int x = topLeft.x; x <= bottomRight.x; x++) {
            for (int z = topLeft.z; z <= bottomRight.z; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);

                float worldX1 = chunkPos.getMinBlockX() - 1;
                float worldX2 = chunkPos.getMaxBlockX() + 1;
                float worldZ1 = chunkPos.getMinBlockZ() - 1;
                float worldZ2 = chunkPos.getMaxBlockZ() + 1;

                float x1 = getRenderX((int) worldX1, mapCenterX, centerX, zoomRenderScale);
                float x2 = getRenderX((int) worldX2, mapCenterX, centerX, zoomRenderScale);
                float z1 = getRenderZ((int) worldZ1, mapCenterZ, centerZ, zoomRenderScale);
                float z2 = getRenderZ((int) worldZ2, mapCenterZ, centerZ, zoomRenderScale);

                CustomColor renderColor =
                        mappedChunks.contains(chunkPos.toLong()) ? CommonColors.GREEN : CommonColors.RED;

                CustomColor topRenderColor =
                        mappedChunks.contains(new ChunkPos(x, z - 1).toLong()) ? CommonColors.GREEN : renderColor;
                CustomColor leftRenderColor =
                        mappedChunks.contains(new ChunkPos(x - 1, z).toLong()) ? CommonColors.GREEN : renderColor;

                // Render the top and left borders of the chunk
                segments.add(new ColoredLineBatchRenderState.Segment(x1, z1, x2, z1, CHUNK_LINE_WIDTH, topRenderColor));
                segments.add(
                        new ColoredLineBatchRenderState.Segment(x1, z1, x1, z2, CHUNK_LINE_WIDTH, leftRenderColor));

                // Render the right border, if the chunk is the rightmost chunk
                if (x == bottomRight.x) {
                    // Check if the chunk on the right is mapped, if it is, render with the correct color
                    CustomColor rightRenderColor =
                            mappedChunks.contains(new ChunkPos(x + 1, z).toLong()) ? CommonColors.GREEN : renderColor;

                    segments.add(new ColoredLineBatchRenderState.Segment(
                            x2, z1, x2, z2, CHUNK_LINE_WIDTH, rightRenderColor));
                }

                // Render the bottom border, if the chunk is the bottommost chunk
                if (z == bottomRight.z) {
                    // Check if the chunk on the top is mapped, if it is, render with the correct color
                    CustomColor bottomRenderColor =
                            mappedChunks.contains(new ChunkPos(x, z + 1).toLong()) ? CommonColors.GREEN : renderColor;

                    segments.add(new ColoredLineBatchRenderState.Segment(
                            x1, z2, x2, z2, CHUNK_LINE_WIDTH, bottomRenderColor));
                }
            }
        }

        if (segments.isEmpty()) return;

        guiGraphics.guiRenderState.submitGuiElement(new ColoredLineBatchRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                segments,
                guiGraphics.scissorStack.peek()));
    }

    public static float getRenderX(int worldX, float mapCenterX, float centerX, float currentZoom) {
        double distanceX = worldX - mapCenterX;
        return (float) (centerX + distanceX * currentZoom);
    }

    public static float getRenderZ(int worldZ, float mapCenterZ, float centerZ, float currentZoom) {
        double distanceZ = worldZ - mapCenterZ;
        return (float) (centerZ + distanceZ * currentZoom);
    }

    private static void renderCircleMaskedTexturedRect(
            GuiGraphics guiGraphics,
            Identifier identifier,
            CustomColor color,
            float x1,
            float y1,
            float x2,
            float y2,
            float u1,
            float u2,
            float v1,
            float v2,
            CircleMask mask) {
        AbstractTexture texture = McUtils.mc().getTextureManager().getTexture(identifier);
        Matrix3x2f pose = new Matrix3x2f(guiGraphics.pose());
        List<Vertex> vertices = clipTexturedRectToCircle(pose, x1, y1, x2, y2, u1, u2, v1, v2, mask);
        if (vertices.isEmpty()) return;

        guiGraphics.guiRenderState.submitGuiElement(new TexturedPolygonRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
                pose,
                vertices,
                color,
                guiGraphics.scissorStack.peek()));
    }

    private static List<Vertex> clipTexturedRectToCircle(
            Matrix3x2f pose,
            float x1,
            float y1,
            float x2,
            float y2,
            float u1,
            float u2,
            float v1,
            float v2,
            CircleMask mask) {
        Matrix3x2f inversePose = new Matrix3x2f(pose).invert();
        List<Vertex> vertices = new ArrayList<>(List.of(
                new Vertex(transform(pose, x1, y1), new Vector2f(u1, v1)),
                new Vertex(transform(pose, x1, y2), new Vector2f(u1, v2)),
                new Vertex(transform(pose, x2, y2), new Vector2f(u2, v2)),
                new Vertex(transform(pose, x2, y1), new Vector2f(u2, v1))));

        for (int i = 0; i < CIRCLE_MASK_SEGMENTS && !vertices.isEmpty(); i++) {
            float startAngleRadians = (float) (Math.PI * 2.0 * i / CIRCLE_MASK_SEGMENTS);
            float endAngleRadians = (float) (Math.PI * 2.0 * (i + 1) / CIRCLE_MASK_SEGMENTS);

            vertices = clipAgainstEdge(vertices, mask.point(startAngleRadians), mask.point(endAngleRadians));
        }

        List<Vertex> localVertices = new ArrayList<>(vertices.size());
        for (Vertex vertex : vertices) {
            localVertices.add(new Vertex(
                    transform(
                            inversePose,
                            vertex.position().x(),
                            vertex.position().y()),
                    vertex.uv()));
        }

        return localVertices;
    }

    private static List<Vertex> clipAgainstEdge(List<Vertex> vertices, Vector2f edgeStart, Vector2f edgeEnd) {
        List<Vertex> result = new ArrayList<>();
        Vertex previous = vertices.getLast();
        boolean previousInside = isInside(previous.position(), edgeStart, edgeEnd);

        for (Vertex current : vertices) {
            boolean currentInside = isInside(current.position(), edgeStart, edgeEnd);
            if (currentInside != previousInside) {
                result.add(intersection(previous, current, edgeStart, edgeEnd));
            }

            if (currentInside) {
                result.add(current);
            }

            previous = current;
            previousInside = currentInside;
        }

        return result;
    }

    private static boolean isInside(Vector2f point, Vector2f edgeStart, Vector2f edgeEnd) {
        float edgeX = edgeEnd.x() - edgeStart.x();
        float edgeY = edgeEnd.y() - edgeStart.y();
        float pointX = point.x() - edgeStart.x();
        float pointY = point.y() - edgeStart.y();

        return edgeX * pointY - edgeY * pointX >= 0f;
    }

    private static Vertex intersection(Vertex start, Vertex end, Vector2f edgeStart, Vector2f edgeEnd) {
        Vector2f line = new Vector2f(end.position()).sub(start.position());
        Vector2f edge = new Vector2f(edgeEnd).sub(edgeStart);
        float denominator = cross(line, edge);

        if (Math.abs(denominator) < 0.00001f) {
            return start;
        }

        float t = cross(new Vector2f(edgeStart).sub(start.position()), edge) / denominator;
        return new Vertex(new Vector2f(start.position()).fma(t, line), new Vector2f(start.uv()).lerp(end.uv(), t));
    }

    private static float cross(Vector2f a, Vector2f b) {
        return a.x() * b.y() - a.y() * b.x();
    }

    private static Vector2f transform(Matrix3x2f matrix, float x, float y) {
        return matrix.transformPosition(new Vector2f(x, y));
    }
}
