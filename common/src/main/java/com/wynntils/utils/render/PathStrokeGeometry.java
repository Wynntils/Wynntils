/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.state.ColoredTriangleBatchRenderState.Triangle;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector2f;

public final class PathStrokeGeometry {
    public static List<Triangle> createTriangles(
            List<Vector2f> vertices, CustomColor color, float width, CustomColor borderColor, float borderWidth) {
        if (width <= 0 || !Float.isFinite(width) || !Float.isFinite(borderWidth)) return List.of();

        List<Vector2f> points = new ArrayList<>();
        for (Vector2f vertex : vertices) {
            if (!Float.isFinite(vertex.x()) || !Float.isFinite(vertex.y())) return List.of();

            if (points.isEmpty() || !points.getLast().equals(vertex)) {
                points.add(vertex);
            }
        }

        if (points.size() < 2) return List.of();

        List<Triangle> triangles = new ArrayList<>();
        if (points.size() == 2) {
            appendSection(
                    triangles,
                    points.getFirst(),
                    points.getFirst().lerp(points.getLast(), 0.5f, new Vector2f()),
                    points.getLast(),
                    color,
                    width,
                    borderColor,
                    borderWidth);

            return triangles;
        }

        for (int i = 1; i < points.size() - 1; i++) {
            Vector2f start = i == 1 ? points.getFirst() : points.get(i - 1).lerp(points.get(i), 0.5f, new Vector2f());
            Vector2f end = i == points.size() - 2
                    ? points.getLast()
                    : points.get(i).lerp(points.get(i + 1), 0.5f, new Vector2f());
            appendSection(triangles, start, points.get(i), end, color, width, borderColor, borderWidth);
        }

        return triangles;
    }

    private static void appendSection(
            List<Triangle> triangles,
            Vector2f start,
            Vector2f joint,
            Vector2f end,
            CustomColor color,
            float width,
            CustomColor borderColor,
            float borderWidth) {
        // Preserve traversal order: each section's border and interior precede the next section.
        // Drawing all borders first would erase the over/under distinction at self-intersections.
        if (borderWidth > 0 && borderColor.a() > 0) {
            appendStroke(triangles, start, joint, end, borderColor, width / 2f + borderWidth);
        }

        if (color.a() > 0) {
            appendStroke(triangles, start, joint, end, color, width / 2f);
        }
    }

    private static void appendStroke(
            List<Triangle> triangles, Vector2f start, Vector2f joint, Vector2f end, CustomColor color, float radius) {
        List<Vector2f> vertices = new ArrayList<>();
        drawTriangles(vertices, start, joint, end, radius);
        for (int i = 0; i + 2 < vertices.size(); i += 3) {
            triangles.add(new Triangle(vertices.get(i), vertices.get(i + 1), vertices.get(i + 2), color));
        }
    }

    private static void drawTriangles(List<Vector2f> vertices, Vector2f p0, Vector2f p1, Vector2f p2, float lineWidth) {
        double dx0 = (double) p1.x() - p0.x();
        double dy0 = (double) p1.y() - p0.y();
        double dx2 = (double) p2.x() - p1.x();
        double dy2 = (double) p2.y() - p1.y();
        double length0 = Math.hypot(dx0, dy0);
        double length2 = Math.hypot(dx2, dy2);
        if (length0 == 0 || length2 == 0) {
            drawStraightSegment(vertices, p0, p2, lineWidth);

            return;
        }

        dx0 /= length0;
        dy0 /= length0;
        dx2 /= length2;
        dy2 /= length2;
        double cross = dx0 * dy2 - dy0 * dx2;
        double dot = dx0 * dx2 + dy0 * dy2;
        if (Math.abs(cross) < 0.00001 && dot > 0) {
            // Straight sections have no join. Intersecting their almost-parallel offset lines
            // amplifies screen-coordinate rounding into spikes as the minimap rotates or moves.
            drawStraightSegment(vertices, p0, p2, lineWidth);

            return;
        }

        double side = cross > 0 ? -1 : 1;
        Vector2f t0 = new Vector2f((float) (-dy0 * side * lineWidth), (float) (dx0 * side * lineWidth));
        Vector2f t2 = new Vector2f((float) (-dy2 * side * lineWidth), (float) (dx2 * side * lineWidth));
        Vector2f anchor = new Vector2f();
        float anchorLength = Float.MAX_VALUE;
        double denominator = 1 + dot;
        if (denominator > 0.000001) {
            // Solve the offset intersection relative to the joint, using normalized directions.
            // This avoids subtracting large absolute screen coordinates in a line intersection.
            anchor.set((float) ((t0.x() + t2.x()) / denominator), (float) ((t0.y() + t2.y()) / denominator));
            anchorLength = anchor.length();
        }

        Vector2f p0p1 = new Vector2f(p0).sub(p1);
        Vector2f p1p2 = new Vector2f(p1).sub(p2);

        if (anchorLength > lineWidth * 4 || anchorLength > p0p1.length() || anchorLength > p1p2.length()) {
            addVertex(vertices, new Vector2f(p0).add(t0));
            addVertex(vertices, new Vector2f(p0).sub(t0));
            addVertex(vertices, new Vector2f(p1).add(t0));

            addVertex(vertices, new Vector2f(p0).sub(t0));
            addVertex(vertices, new Vector2f(p1).add(t0));
            addVertex(vertices, new Vector2f(p1).sub(t0));

            drawRoundJoint(p1, new Vector2f(p1).add(t0), new Vector2f(p1).add(t2), p2, vertices);

            addVertex(vertices, new Vector2f(p2).add(t2));
            addVertex(vertices, new Vector2f(p1).sub(t2));
            addVertex(vertices, new Vector2f(p1).add(t2));

            addVertex(vertices, new Vector2f(p2).add(t2));
            addVertex(vertices, new Vector2f(p1).sub(t2));
            addVertex(vertices, new Vector2f(p2).sub(t2));
        } else {
            addVertex(vertices, new Vector2f(p0).add(t0));
            addVertex(vertices, new Vector2f(p0).sub(t0));
            addVertex(vertices, new Vector2f(p1).sub(anchor));

            addVertex(vertices, new Vector2f(p0).add(t0));
            addVertex(vertices, new Vector2f(p1).sub(anchor));
            addVertex(vertices, new Vector2f(p1).add(t0));

            Vector2f rP0 = new Vector2f(p1).add(t0);
            Vector2f rP1 = new Vector2f(p1).add(t2);
            Vector2f rP2 = new Vector2f(p1).sub(anchor);

            addVertex(vertices, rP0);
            addVertex(vertices, p1);
            addVertex(vertices, rP2);

            drawRoundJoint(p1, rP0, rP1, rP2, vertices);

            addVertex(vertices, p1);
            addVertex(vertices, rP1);
            addVertex(vertices, rP2);

            addVertex(vertices, new Vector2f(p2).add(t2));
            addVertex(vertices, new Vector2f(p1).sub(anchor));
            addVertex(vertices, new Vector2f(p1).add(t2));

            addVertex(vertices, new Vector2f(p2).add(t2));
            addVertex(vertices, new Vector2f(p1).sub(anchor));
            addVertex(vertices, new Vector2f(p2).sub(t2));
        }
    }

    private static void drawStraightSegment(List<Vector2f> vertices, Vector2f start, Vector2f end, float radius) {
        double dx = (double) end.x() - start.x();
        double dy = (double) end.y() - start.y();
        double length = Math.hypot(dx, dy);
        if (length == 0) return;

        Vector2f normal = new Vector2f((float) (-dy / length * radius), (float) (dx / length * radius));
        Vector2f a = start.add(normal, new Vector2f());
        Vector2f b = start.sub(normal, new Vector2f());
        Vector2f c = end.add(normal, new Vector2f());
        Vector2f d = end.sub(normal, new Vector2f());
        addVertex(vertices, a);
        addVertex(vertices, b);
        addVertex(vertices, c);
        addVertex(vertices, b);
        addVertex(vertices, d);
        addVertex(vertices, c);
    }

    private static void drawRoundJoint(
            Vector2f center, Vector2f p0, Vector2f p1, Vector2f nextPointInLine, List<Vector2f> vertices) {
        float radius = new Vector2f(center).sub(p0).length();

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
            Vector2f r1 = new Vector2f(center).sub(nextPointInLine);

            if (r1.x() == 0) {
                if (r1.y() > 0) {
                    angleDiff = -angleDiff;
                }
            } else if (r1.x() >= -0.001f) {
                angleDiff = -angleDiff;
            }
        }

        // Bound tessellation even if a provider supplies an unusually large stroke width.
        int nSegments = Math.clamp((int) Math.ceil(Math.abs(angleDiff) * radius / 2), 1, 64);

        float angleInc = angleDiff / nSegments;

        for (int i = 0; i < nSegments; i++) {
            addVertex(vertices, center);
            addVertex(vertices, new Vector2f((float) (center.x() + radius * Math.cos(orgAngle0 + angleInc * i)), (float)
                    (center.y() + radius * Math.sin(orgAngle0 + angleInc * i))));
            addVertex(
                    vertices,
                    new Vector2f((float) (center.x() + radius * Math.cos(orgAngle0 + angleInc * (1 + i))), (float)
                            (center.y() + radius * Math.sin(orgAngle0 + angleInc * (1 + i)))));
        }
    }

    private static void addVertex(List<Vector2f> vertices, Vector2f pos) {
        vertices.add(new Vector2f(pos));
    }
}
