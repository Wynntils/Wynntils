/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

public final class PathGeometry {
    public static float distanceSquaredToSegment(float x, float y, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float lengthSquared = dx * dx + dy * dy;
        float t = lengthSquared == 0 ? 0 : Math.clamp(((x - ax) * dx + (y - ay) * dy) / lengthSquared, 0f, 1f);
        float distanceX = x - (ax + t * dx);
        float distanceY = y - (ay + t * dy);

        return distanceX * distanceX + distanceY * distanceY;
    }

    public static boolean intersectsSegment(BoundingShape shape, float ax, float ay, float bx, float by) {
        if (shape instanceof BoundingCircle circle) {
            return distanceSquaredToSegment(circle.x(), circle.z(), ax, ay, bx, by)
                    <= circle.radius() * circle.radius();
        }

        if (shape instanceof BoundingBox box) {
            // Clip the segment's parameter interval against each of the box's two axes.
            float enter = 0;
            float exit = 1;
            float dx = bx - ax;
            float dy = by - ay;
            if (dx == 0) {
                if (ax < box.x1() || ax > box.x2()) return false;
            } else {
                float t1 = (box.x1() - ax) / dx;
                float t2 = (box.x2() - ax) / dx;
                enter = Math.max(enter, Math.min(t1, t2));
                exit = Math.min(exit, Math.max(t1, t2));
            }

            if (dy == 0) {
                if (ay < box.z1() || ay > box.z2()) return false;
            } else {
                float t1 = (box.z1() - ay) / dy;
                float t2 = (box.z2() - ay) / dy;
                enter = Math.max(enter, Math.min(t1, t2));
                exit = Math.min(exit, Math.max(t1, t2));
            }

            return enter <= exit;
        }

        // BoundingPolygon guarantees counterclockwise, convex vertices. Clip against its inward half-planes.
        BoundingPolygon polygon = (BoundingPolygon) shape;
        float enter = 0;
        float exit = 1;
        for (int i = 0; i < polygon.vertices().size(); i++) {
            Vector2f a = polygon.vertices().get(i);
            Vector2f b = polygon.vertices().get((i + 1) % polygon.vertices().size());
            float edgeX = b.x() - a.x();
            float edgeY = b.y() - a.y();
            float start = edgeX * (ay - a.y()) - edgeY * (ax - a.x());
            float change = edgeX * (by - ay) - edgeY * (bx - ax);
            if (change == 0) {
                if (start < 0) return false;
            } else if (change > 0) {
                enter = Math.max(enter, -start / change);
            } else {
                exit = Math.min(exit, -start / change);
            }

            if (enter > exit) return false;
        }

        return true;
    }

    public static List<Vec3> simplifyForMap(List<Vec3> points) {
        if (points.size() < 3) return points;

        boolean[] keep = new boolean[points.size()];
        keep[0] = true;
        keep[points.size() - 1] = true;
        ArrayDeque<int[]> pending = new ArrayDeque<>();
        pending.push(new int[] {0, points.size() - 1});
        while (!pending.isEmpty()) {
            int[] span = pending.pop();
            Vec3 a = points.get(span[0]);
            Vec3 b = points.get(span[1]);
            double dx = b.x() - a.x();
            double dz = b.z() - a.z();
            double lengthSquared = dx * dx + dz * dz;
            double farthest = 0.25; // Half-block Douglas-Peucker tolerance, ignoring height.
            int split = -1;
            for (int i = span[0] + 1; i < span[1]; i++) {
                Vec3 p = points.get(i);
                double t = lengthSquared == 0
                        ? 0
                        : Math.clamp(((p.x() - a.x()) * dx + (p.z() - a.z()) * dz) / lengthSquared, 0, 1);
                double distanceX = p.x() - a.x() - t * dx;
                double distanceZ = p.z() - a.z() - t * dz;
                double distanceSquared = distanceX * distanceX + distanceZ * distanceZ;
                if (distanceSquared > farthest) {
                    farthest = distanceSquared;
                    split = i;
                }
            }

            if (split >= 0) {
                keep[split] = true;
                pending.push(new int[] {span[0], split});
                pending.push(new int[] {split, span[1]});
            }
        }

        List<Vec3> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (keep[i]) {
                result.add(points.get(i));
            }
        }

        return List.copyOf(result);
    }

    public static Vector2f midpoint(List<Vector2f> vertices) {
        float length = 0;
        for (int i = 1; i < vertices.size(); i++) {
            length += vertices.get(i - 1).distance(vertices.get(i));
        }

        float remaining = length / 2f;
        for (int i = 1; i < vertices.size(); i++) {
            Vector2f a = vertices.get(i - 1);
            Vector2f b = vertices.get(i);
            float segmentLength = a.distance(b);
            if (segmentLength > 0 && remaining <= segmentLength) {
                return a.lerp(b, remaining / segmentLength, new Vector2f());
            }

            remaining -= segmentLength;
        }

        return new Vector2f(vertices.getFirst());
    }
}
