/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BinaryOperator;
import net.minecraft.world.phys.Vec3;

/**
 * General bounding box interface.
 *
 * @author Kepler-17c
 */
public interface IBoundingBox {

    default Vec3 getLower() {
        return this.getBounds().getLower();
    }

    default Vec3 getUpper() {
        return this.getBounds().getUpper();
    }

    default Vec3 size() {
        return this.getBounds().size();
    }

    default Vec3 mid() {
        return this.getBounds().mid();
    }

    default double squareDistance(final Vec3 point) {
        return AxisAlignedBoundingBox.squareDistance(AxisAlignedBoundingBox.fromIBoundingBox(this), point);
    }

    default boolean contains(final IBoundingBox other) {
        return this.getBounds().contains(other);
    }

    default boolean contains(final Vec3 point) {
        return this.getBounds().contains(point);
    }

    default boolean intersects(IBoundingBox other) {
        return this.getBounds().intersects(other);
    }

    default boolean intersectsWithoutBorder(IBoundingBox other) {
        return this.getBounds().intersectsWithoutBorder(other);
    }

    default boolean hasInnerPoint(final Vec3 point) {
        return this.getBounds().hasInnerPoint(point);
    }

    default double volume() {
        return this.getBounds().volume();
    }

    default Pair<Vec3, Vec3> definingPoints() {
        return new Pair<>(this.getLower(), this.getUpper());
    }

    AxisAlignedBoundingBox getBounds();

    class AxisAlignedBoundingBox implements IBoundingBox {
        private static final Vec3 NEUTRAL_LOWER = doubleToVec3(Double.POSITIVE_INFINITY);
        private static final Vec3 NEUTRAL_UPPER = doubleToVec3(Double.NEGATIVE_INFINITY);
        public static final AxisAlignedBoundingBox INFINITE = new AxisAlignedBoundingBox(
                doubleToVec3(Double.NEGATIVE_INFINITY), doubleToVec3(Double.POSITIVE_INFINITY));

        private Vec3 lower;
        private Vec3 upper;
        private Vec3 size;
        private Vec3 mid;

        public AxisAlignedBoundingBox() {
            this.lower = NEUTRAL_LOWER;
            this.upper = NEUTRAL_UPPER;
            this.size = doubleToVec3(0);
            this.mid = doubleToVec3(0);
        }

        public AxisAlignedBoundingBox(final Vec3... points) {
            final MutablePair<Vec3, Vec3> definingPoints = Arrays.stream(points)
                    .collect(
                            () -> new MutablePair<>(NEUTRAL_LOWER, NEUTRAL_UPPER),
                            (acc, val) -> {
                                acc.a = minCoords(acc.a, val);
                                acc.b = maxCoords(acc.b, val);
                            },
                            (acc, val) -> {
                                acc.a = minCoords(acc.a, val.a);
                                acc.b = maxCoords(acc.b, val.b);
                            });
            this.lower = definingPoints.a;
            this.upper = definingPoints.b;
            this.updateSizeAndMid();
        }

        public AxisAlignedBoundingBox(final Collection<? extends IBoundingBox> points) {
            final MutablePair<Vec3, Vec3> definingPoints = points.stream()
                    .map(IBoundingBox::definingPoints)
                    .collect(
                            () -> new MutablePair<>(NEUTRAL_LOWER, NEUTRAL_UPPER),
                            (acc, val) -> {
                                acc.a = minCoords(acc.a, val.a);
                                acc.b = minCoords(acc.b, val.b);
                            },
                            (acc, val) -> {
                                acc.a = minCoords(acc.a, val.a);
                                acc.b = minCoords(acc.b, val.b);
                            });
            this.lower = definingPoints.a;
            this.upper = definingPoints.b;
            this.updateSizeAndMid();
        }

        private static Vec3 doubleToVec3(final double d) {
            return new Vec3(d, d, d);
        }

        public static AxisAlignedBoundingBox fromIBoundingBox(final IBoundingBox iaabb) {
            return new AxisAlignedBoundingBox(iaabb.getLower(), iaabb.getUpper());
        }

        private static Vec3 minCoords(final Vec3 a, final Vec3 b) {
            return new Vec3(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
        }

        private static Vec3 maxCoords(final Vec3 a, final Vec3 b) {
            return new Vec3(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
        }

        public static AxisAlignedBoundingBox mergeBounds(final IBoundingBox a, final IBoundingBox b) {
            final AxisAlignedBoundingBox result = new AxisAlignedBoundingBox();
            result.lower = minCoords(a.getLower(), b.getLower());
            result.upper = maxCoords(a.getUpper(), b.getUpper());
            result.updateSizeAndMid();
            return result;
        }

        private void updateSizeAndMid() {
            size = this.upper.subtract(lower);
            mid = size.scale(0.5).add(lower);
        }

        public void add(final Vec3 point) {
            this.lower = minCoords(this.lower, point);
            this.upper = maxCoords(this.upper, point);
            this.updateSizeAndMid();
        }

        @Override
        public Vec3 getLower() {
            return this.lower;
        }

        @Override
        public Vec3 getUpper() {
            return this.upper;
        }

        public double getMinX() {
            return this.lower.x;
        }

        public double getMinY() {
            return this.lower.y;
        }

        public double getMinZ() {
            return this.lower.z;
        }

        public double getMaxX() {
            return this.upper.x;
        }

        public double getMaxY() {
            return this.upper.y;
        }

        public double getMaxZ() {
            return this.upper.z;
        }

        @Override
        public Vec3 size() {
            return this.size;
        }

        @Override
        public Vec3 mid() {
            return this.mid;
        }

        @Override
        public double squareDistance(final Vec3 point) {
            return squareDistance(this, point);
        }

        @Override
        public boolean contains(final IBoundingBox other) {
            final Vec3 otherLower = other.getLower();
            final Vec3 otherUpper = other.getUpper();
            return this.lower.x <= otherLower.x
                    && this.lower.y <= otherLower.y
                    && this.lower.z <= otherLower.z
                    && otherUpper.x <= this.upper.x
                    && otherUpper.y <= this.upper.y
                    && otherUpper.z <= this.upper.z;
        }

        @Override
        public boolean contains(Vec3 point) {
            return this.lower.x <= point.x
                    && this.lower.y <= point.y
                    && this.lower.z <= point.z
                    && point.x <= this.upper.x
                    && point.y <= this.upper.y
                    && point.z <= this.upper.z;
        }

        @Override
        public boolean intersects(IBoundingBox other) {
            return this.contains(other.getLower())
                    || this.contains(other.getUpper())
                    || other.contains(this.lower)
                    || other.contains(this.upper);
        }

        @Override
        public boolean intersectsWithoutBorder(IBoundingBox other) {
            return this.hasInnerPoint(other.getLower())
                    || this.hasInnerPoint(other.getUpper())
                    || other.hasInnerPoint(this.lower)
                    || other.hasInnerPoint(this.upper);
        }

        @Override
        public boolean hasInnerPoint(Vec3 point) {
            return this.lower.x < point.x
                    && this.lower.y < point.y
                    && this.lower.z < point.z
                    && point.x < this.upper.x
                    && point.y < this.upper.y
                    && point.z < this.upper.z;
        }

        @Override
        public double volume() {
            return this.size.x * this.size.y * this.size.z;
        }

        @Override
        public AxisAlignedBoundingBox getBounds() {
            return this;
        }

        @Override
        public String toString() {
            return "[" + this.lower + ", " + this.upper + "]";
        }

        private static Vec3 mergePoints(final Vec3 a, final Vec3 b, final BinaryOperator<Double> mergeFunction) {
            return new Vec3(
                    mergeFunction.apply(a.x, b.x), mergeFunction.apply(a.y, b.y), mergeFunction.apply(a.z, b.z));
        }

        private static double squareDistance(final AxisAlignedBoundingBox bb, final Vec3 point) {
            // view along coords as 1D intervals
            // separate calculation works because of super-position
            final double xDist =
                    point.x < bb.lower.x ? bb.lower.x - point.x : point.x > bb.upper.x ? point.x - bb.upper.x : 0;
            final double yDist =
                    point.y < bb.lower.y ? bb.lower.y - point.y : point.y > bb.upper.y ? point.y - bb.upper.y : 0;
            final double zDist =
                    point.z < bb.lower.z ? bb.lower.z - point.z : point.z > bb.upper.z ? point.z - bb.upper.z : 0;
            // combine 1D linear distances to 3D squared distance
            return (xDist * xDist) + (yDist * yDist) + (zDist * zDist);
        }
    }
}
