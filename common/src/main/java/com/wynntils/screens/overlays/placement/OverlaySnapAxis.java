/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class OverlaySnapAxis {
    private static final double SNAP_DISTANCE = 1;
    private static final double RELEASE_DISTANCE = 6;

    private Float target;
    private int snappedPoint;
    private double pointerOffset;

    public double snap(
            double drag,
            double[] edges,
            Collection<Float> targets,
            Double center,
            Collection<Float> centerTargets,
            double minDrag,
            double maxDrag) {
        List<SnapPoint> points = new ArrayList<>();
        for (double edge : edges) {
            points.add(new SnapPoint(edge, targets));
        }
        if (center != null) {
            points.add(new SnapPoint(center, centerTargets));
        }

        double proposedDrag = Math.clamp(drag + pointerOffset, minDrag, maxDrag);
        if (target != null) {
            double snappedDrag = target - points.get(snappedPoint).position();
            if (Math.abs(proposedDrag - snappedDrag) <= RELEASE_DISTANCE
                    && snappedDrag >= minDrag
                    && snappedDrag <= maxDrag) {
                pointerOffset = proposedDrag - snappedDrag;
                return snappedDrag;
            }

            // Release to the pointer's position without immediately catching another guide.
            reset();
            return proposedDrag;
        }

        if (proposedDrag == 0) return 0;

        double nearestDistance = Double.POSITIVE_INFINITY;
        double result = proposedDrag;
        for (int i = 0; i < points.size(); i++) {
            SnapPoint point = points.get(i);
            for (float candidate : point.targets()) {
                double snappedDrag = candidate - point.position();
                if (snappedDrag < minDrag || snappedDrag > maxDrag) continue;

                double distance = Math.abs(snappedDrag - proposedDrag);
                if (distance <= SNAP_DISTANCE
                        && (distance < nearestDistance
                                || (distance == nearestDistance && (target == null || candidate < target)))) {
                    nearestDistance = distance;
                    result = snappedDrag;
                    target = candidate;
                    snappedPoint = i;
                }
            }
        }

        pointerOffset = proposedDrag - result;
        return result;
    }

    public Float getTarget() {
        return target;
    }

    public void reset() {
        target = null;
        snappedPoint = 0;
        pointerOffset = 0;
    }

    private record SnapPoint(double position, Collection<Float> targets) {}
}
