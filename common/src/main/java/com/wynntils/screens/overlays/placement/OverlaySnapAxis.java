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

    private SnapTarget target;
    private int snappedPoint;
    private double pointerOffset;

    public double snap(
            double drag,
            double[] edges,
            Collection<SnapTarget> targets,
            Double center,
            Collection<SnapTarget> centerTargets,
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
            double snappedDrag = target.position() - points.get(snappedPoint).position();
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
            for (SnapTarget candidate : point.targets()) {
                double snappedDrag = candidate.position() - point.position();
                if (snappedDrag < minDrag || snappedDrag > maxDrag) continue;

                double distance = Math.abs(snappedDrag - proposedDrag);
                if (distance <= SNAP_DISTANCE
                        && (distance < nearestDistance || (distance == nearestDistance && preferTarget(candidate)))) {
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

    public SnapTarget getTarget() {
        return target;
    }

    public void reset() {
        target = null;
        snappedPoint = 0;
        pointerOffset = 0;
    }

    private boolean preferTarget(SnapTarget candidate) {
        if (target == null || candidate.position() < target.position()) return true;
        // Prefer the screen guide when an overlay lies on exactly the same line.
        return candidate.position() == target.position() && candidate.screen() && !target.screen();
    }

    record SnapTarget(float position, boolean screen) {}

    private record SnapPoint(double position, Collection<SnapTarget> targets) {}
}
