/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import java.util.Collection;

final class OverlaySnapAxis {
    private static final double SNAP_DISTANCE = 1;
    private static final double RELEASE_DISTANCE = 6;

    private Float target;
    private int snappedEdge;
    private double pointerOffset;

    public double snap(double drag, double[] edges, Collection<Float> targets, double minDrag, double maxDrag) {
        double proposedDrag = Math.clamp(drag + pointerOffset, minDrag, maxDrag);
        if (target != null) {
            double snappedDrag = target - edges[snappedEdge];
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
        for (int i = 0; i < edges.length; i++) {
            for (float candidate : targets) {
                double snappedDrag = candidate - edges[i];
                if (snappedDrag < minDrag || snappedDrag > maxDrag) continue;

                double distance = Math.abs(snappedDrag - proposedDrag);
                if (distance <= SNAP_DISTANCE
                        && (distance < nearestDistance
                                || (distance == nearestDistance && (target == null || candidate < target)))) {
                    nearestDistance = distance;
                    result = snappedDrag;
                    target = candidate;
                    snappedEdge = i;
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
        snappedEdge = 0;
        pointerOffset = 0;
    }
}
