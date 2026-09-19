/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.state.ColoredLineBatchRenderState;
import com.wynntils.utils.render.state.ColoredTriangleBatchRenderState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

public final class PolygonGeometry {
    public static List<ColoredTriangleBatchRenderState.Triangle> createFillTriangles(
            List<Vector2f> vertices, List<CustomColor> colors) {
        if (colors.isEmpty()) return List.of();
        if (colors.size() > 1) {
            return createMulticoloredFillTriangles(vertices, colors);
        }

        CustomColor color = colors.getFirst();
        if (color == CustomColor.NONE) return List.of();

        // A simple triangle fan decomposition is enough for a single color
        List<ColoredTriangleBatchRenderState.Triangle> triangles = new ArrayList<>(vertices.size() - 2);
        Vector2f firstVertex = vertices.getFirst();
        for (int i = 1; i < vertices.size() - 1; i++) {
            Vector2f v1 = vertices.get(i);
            Vector2f v2 = vertices.get(i + 1);
            triangles.add(new ColoredTriangleBatchRenderState.Triangle(firstVertex, v1, v2, color));
        }

        return triangles;
    }

    public static List<ColoredLineBatchRenderState.Segment> createBorderSegments(
            List<Vector2f> vertices, List<CustomColor> colors, float borderWidth) {
        if (colors.isEmpty() || borderWidth <= 0f) return List.of();
        if (colors.size() > 1) {
            return createMulticoloredBorderSegments(vertices, colors, borderWidth);
        }

        CustomColor color = colors.getFirst();
        if (color == CustomColor.NONE) return List.of();

        List<ColoredLineBatchRenderState.Segment> segments = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            Vector2f v1 = vertices.get(i);
            Vector2f v2 = vertices.get((i + 1) % vertices.size());
            segments.add(new ColoredLineBatchRenderState.Segment(v1.x(), v1.y(), v2.x(), v2.y(), borderWidth, color));
        }

        return segments;
    }

    private static List<ColoredTriangleBatchRenderState.Triangle> createMulticoloredFillTriangles(
            List<Vector2f> vertices, List<CustomColor> colors) {
        PolygonBounds bounds = getPolygonBounds(vertices);
        float splitX = bounds.width() / (colors.size() - 1);
        if (splitX <= 0f || bounds.height() <= 0f) {
            return List.of();
        }

        float yFactor = splitX / bounds.height();
        List<ColoredTriangleBatchRenderState.Triangle> triangles = new ArrayList<>((vertices.size() - 2) * 2);
        Vector2f firstVertex = vertices.getFirst();

        for (int i = 1; i < vertices.size() - 1; i++) {
            Vector2f v1 = vertices.get(i);
            Vector2f v2 = vertices.get(i + 1);
            float p0 = getDiagonalPosition(firstVertex, bounds, yFactor);
            float p1 = getDiagonalPosition(v1, bounds, yFactor);
            float p2 = getDiagonalPosition(v2, bounds, yFactor);
            float minPosition = Math.min(p0, Math.min(p1, p2));
            float maxPosition = Math.max(p0, Math.max(p1, p2));
            int firstColor = Mth.clamp((int) Math.floor(minPosition / splitX), 0, colors.size() - 1);
            int lastColor = Mth.clamp((int) Math.floor(maxPosition / splitX), 0, colors.size() - 1);

            for (int colorIndex = firstColor; colorIndex <= lastColor; colorIndex++) {
                CustomColor color = colors.get(colorIndex);
                if (color == CustomColor.NONE) continue;

                List<Vector2f> clipped = new ArrayList<>(List.of(firstVertex, v1, v2));
                clipped = clipToDiagonalBoundary(clipped, colorIndex * splitX, true, bounds, yFactor);
                clipped = clipToDiagonalBoundary(clipped, (colorIndex + 1) * splitX, false, bounds, yFactor);

                if (clipped.size() < 3) continue;

                Vector2f clippedFirst = clipped.getFirst();
                for (int vertexIndex = 1; vertexIndex < clipped.size() - 1; vertexIndex++) {
                    Vector2f clippedV1 = clipped.get(vertexIndex);
                    Vector2f clippedV2 = clipped.get(vertexIndex + 1);

                    if (triangleArea(clippedFirst, clippedV1, clippedV2) > 0.0001f) {
                        triangles.add(new ColoredTriangleBatchRenderState.Triangle(
                                clippedFirst, clippedV1, clippedV2, color));
                    }
                }
            }
        }

        return triangles;
    }

    private static List<Vector2f> clipToDiagonalBoundary(
            List<Vector2f> input, float boundary, boolean keepGreater, PolygonBounds bounds, float yFactor) {
        if (input.isEmpty()) return input;

        List<Vector2f> output = new ArrayList<>(input.size() + 1);
        Vector2f previous = input.getLast();
        float previousPosition = getDiagonalPosition(previous, bounds, yFactor);
        boolean previousInside = keepGreater ? previousPosition >= boundary : previousPosition <= boundary;

        for (Vector2f current : input) {
            float currentPosition = getDiagonalPosition(current, bounds, yFactor);
            boolean currentInside = keepGreater ? currentPosition >= boundary : currentPosition <= boundary;

            if (currentInside != previousInside) {
                float amount = (boundary - previousPosition) / (currentPosition - previousPosition);
                output.add(new Vector2f(
                        Mth.lerp(amount, previous.x(), current.x()), Mth.lerp(amount, previous.y(), current.y())));
            }

            if (currentInside) {
                output.add(current);
            }

            previous = current;
            previousPosition = currentPosition;
            previousInside = currentInside;
        }

        return output;
    }

    private static List<ColoredLineBatchRenderState.Segment> createMulticoloredBorderSegments(
            List<Vector2f> vertices, List<CustomColor> colors, float borderWidth) {
        PolygonBounds bounds = getPolygonBounds(vertices);
        float splitX = bounds.width() / (colors.size() - 1);
        if (splitX <= 0f || bounds.height() <= 0f) {
            return List.of();
        }

        float yFactor = splitX / bounds.height();
        List<ColoredLineBatchRenderState.Segment> segments = new ArrayList<>(vertices.size() + colors.size() - 1);

        // Split the polygon's outside edge wherever it crosses a color boundary.
        for (int i = 0; i < vertices.size(); i++) {
            Vector2f v1 = vertices.get(i);
            Vector2f v2 = vertices.get((i + 1) % vertices.size());
            float p1 = getDiagonalPosition(v1, bounds, yFactor);
            float p2 = getDiagonalPosition(v2, bounds, yFactor);
            List<Float> cuts = new ArrayList<>(colors.size() + 1);
            cuts.add(0f);

            for (int boundaryIndex = 1; boundaryIndex < colors.size(); boundaryIndex++) {
                float boundary = boundaryIndex * splitX;

                if ((p1 < boundary && p2 > boundary) || (p2 < boundary && p1 > boundary)) {
                    cuts.add((boundary - p1) / (p2 - p1));
                }
            }
            cuts.add(1f);
            cuts.sort(Float::compare);

            for (int cutIndex = 0; cutIndex < cuts.size() - 1; cutIndex++) {
                float start = cuts.get(cutIndex);
                float end = cuts.get(cutIndex + 1);
                float middlePosition = Mth.lerp((start + end) / 2f, p1, p2);
                int colorIndex = Mth.clamp((int) Math.floor(middlePosition / splitX), 0, colors.size() - 1);
                CustomColor color = colors.get(colorIndex);
                if (color == CustomColor.NONE) continue;

                segments.add(new ColoredLineBatchRenderState.Segment(
                        Mth.lerp(start, v1.x(), v2.x()),
                        Mth.lerp(start, v1.y(), v2.y()),
                        Mth.lerp(end, v1.x(), v2.x()),
                        Mth.lerp(end, v1.y(), v2.y()),
                        borderWidth,
                        color));
            }
        }

        // Add the diagonal dividers. Pairing sorted intersections also handles concave polygons.
        for (int boundaryIndex = 1; boundaryIndex < colors.size(); boundaryIndex++) {
            float boundary = boundaryIndex * splitX;
            List<Vector2f> intersections = new ArrayList<>();
            for (int i = 0; i < vertices.size(); i++) {
                Vector2f v1 = vertices.get(i);
                Vector2f v2 = vertices.get((i + 1) % vertices.size());
                float p1 = getDiagonalPosition(v1, bounds, yFactor);
                float p2 = getDiagonalPosition(v2, bounds, yFactor);

                // The half-open comparison counts a boundary vertex only once.
                if ((p1 < boundary && p2 >= boundary) || (p2 < boundary && p1 >= boundary)) {
                    float amount = (boundary - p1) / (p2 - p1);
                    intersections.add(new Vector2f(Mth.lerp(amount, v1.x(), v2.x()), Mth.lerp(amount, v1.y(), v2.y())));
                }
            }

            intersections.sort((first, second) -> {
                int yComparison = Float.compare(first.y(), second.y());
                return yComparison != 0 ? yComparison : Float.compare(first.x(), second.x());
            });
            CustomColor color = colors.get(boundaryIndex);
            if (color == CustomColor.NONE) continue;

            for (int i = 0; i + 1 < intersections.size(); i += 2) {
                Vector2f start = intersections.get(i);
                Vector2f end = intersections.get(i + 1);
                segments.add(new ColoredLineBatchRenderState.Segment(
                        start.x(), start.y(), end.x(), end.y(), borderWidth, color));
            }
        }

        return segments;
    }

    private static float getDiagonalPosition(Vector2f vertex, PolygonBounds bounds, float yFactor) {
        return vertex.x() - bounds.minX() + (vertex.y() - bounds.minY()) * yFactor;
    }

    private static float triangleArea(Vector2f v0, Vector2f v1, Vector2f v2) {
        return Math.abs((v1.x() - v0.x()) * (v2.y() - v0.y()) - (v1.y() - v0.y()) * (v2.x() - v0.x()));
    }

    private static PolygonBounds getPolygonBounds(List<Vector2f> vertices) {
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

        return new PolygonBounds(minX, minY, maxX - minX, maxY - minY);
    }

    private record PolygonBounds(float minX, float minY, float width, float height) {}
}
