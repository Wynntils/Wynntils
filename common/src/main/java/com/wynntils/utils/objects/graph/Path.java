/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.graph;

import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.Pair;
import com.wynntils.utils.objects.Referencable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.world.phys.Vec3;

/**
 * A simple mutable path to be used in a graph structure.
 *
 * @author Kepler-17c
 */
class Path implements IBoundingBox, Referencable {
    /**
     * Serialisation ID.
     */
    private final UUID uuid;
    /**
     * All points of the path, including start- and end-node.
     */
    private final List<Vec3> points;
    /**
     * Bounds of the path's points.
     */
    private AxisAlignedBoundingBox bounds;
    /**
     * Same path with reversed point order.
     */
    private Path reversed;
    /**
     * Sum of all line segments.
     */
    private double length;
    /**
     * Node at the start of the path.
     */
    private Node startNode;
    /**
     * Node at the end of the path.
     */
    private Node endNode;
    /**
     * Whether this path is directed or bidirectional.
     */
    private boolean directed;

    /**
     * Deserialisation constructor with given ID.
     *
     * @param uuid
     *            for serialisation.
     * @param startNode
     *            of the path.
     * @param points
     *            between the nodes.
     * @param endNode
     *            of the path.
     */
    public Path(final UUID uuid, final Node startNode, final List<Vec3> points, final Node endNode) {
        this.uuid = uuid;
        this.bounds = new AxisAlignedBoundingBox(points.toArray(new Vec3[0]));
        this.bounds.add(startNode.getPosition());
        this.bounds.add(endNode.getPosition());
        this.startNode = startNode;
        this.endNode = endNode;
        this.points = new ArrayList<>();
        this.points.add(startNode.getPosition());
        this.points.addAll(points);
        this.points.add(endNode.getPosition());
        this.updateLength();
        this.directed = false;
    }

    /**
     * Helper constructor for path-splitting.
     *
     * @param startNode
     *            of the path.
     * @param points
     *            between nodes.
     * @param endNode
     *            of the path.
     */
    private Path(final Node startNode, final List<Vec3> points, final Node endNode) {
        this(UUID.randomUUID(), startNode, points, endNode);
    }

    /**
     * Base constructor for a new path.
     *
     * @param startNode
     *            of the path.
     * @param endNode
     *            of the path.
     */
    public Path(final Node startNode, final Node endNode) {
        this(startNode, Collections.emptyList(), endNode);
    }

    /**
     * Helper constructor for reverse paths. It makes a shallow copy to keep the instances linked.
     *
     * @param source
     *            to create the shallow copy from.
     */
    private Path(final Path source) {
        this.uuid = UUID.randomUUID();
        this.points = source.points;
        this.startNode = source.startNode;
        this.endNode = source.endNode;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Gets the directionality of the path.
     *
     * @return Whether the path is directed.
     */
    public boolean isDirected() {
        return this.directed;
    }

    /**
     * Tries to append a point to the path.
     * <p>
     * The end-node has to be free and will be moved.
     * </p>
     *
     * @param point
     *            to be appended.
     * @return Whether appending succeeded.
     */
    public boolean appendPoint(final Vec3 point) {
        if (this.endNode.degree() != 1) {
            return false;
        }
        this.bounds.add(point);
        this.points.add(point);
        this.endNode.setPosition(point);
        this.updateLength();
        return true;
    }

    /**
     * Tries to prepend a point to the path.
     * <p>
     * The start-node has to be free and will be moved.
     * </p>
     *
     * @param point
     *            to be prepended.
     * @return Whether prepending succeeded.
     */
    public boolean prependPoint(final Vec3 point) {
        if (this.startNode.degree() != 1) {
            return false;
        }
        this.bounds.add(point);
        this.points.add(0, point);
        this.startNode.setPosition(point);
        this.updateLength();
        return true;
    }

    /**
     * Tries to insert a point on the path.
     *
     * @param point
     *            to be inserted.
     * @return Whether inserting succeeded.
     */
    public boolean insertPoint(final Vec3 point) {
        // get the nearest point on path, to find neighbours later
        final Pair<Integer, Vec3> pathPointPair = this.getNearestPointOnPath(point);
        final int pathIndex = pathPointPair.a;
        final Vec3 pathPoint = pathPointPair.b;
        if (pathIndex == 0 || pathIndex == this.points.size() - 1) { // next to a node, might lie outside
            final Vec3 v0 = this.points.get(pathIndex).subtract(pathPoint);
            final Vec3 v1 =
                    this.points.get(pathIndex + (pathIndex == 0 ? 1 : -1)).subtract(pathPoint);
            if (v0.dot(v1) > 0) { // is outside the path -> try to append or prepend
                if (pathIndex == 0 ? this.prependPoint(point) : this.appendPoint(point)) {
                    this.bounds.add(point);
                    this.updateLength();
                }
            }
        }
        // is on the path
        this.points.add(pathIndex + 1, point);
        this.bounds.add(point);
        this.updateLength();
        return true;
    }

    /**
     * Removes a point from the path. The point has to be an inner path point or a free node (degree 1).
     *
     * @param index
     *            of the point to be removed.
     * @return Whether removing succeeded.
     */
    public boolean deletePoint(final int index) {
        if (index < 0 || index >= this.points.size() || this.points.size() <= 2) {
            return false;
        } else if ((index == 0 && this.startNode.degree() != 1)
                || (index == this.points.size() - 1 && this.endNode.degree() != 1)) {
            return false;
        } else {
            this.points.remove(index);
            if (index == 0) {
                this.startNode.setPosition(this.points.get(index));
            } else if (index == this.points.size()) {
                this.endNode.setPosition(this.points.get(index - 1));
            }
            this.updateLength();
            this.bounds = new AxisAlignedBoundingBox(this.points.toArray(new Vec3[0]));
            return true;
        }
    }

    /**
     * Attaches the path to a node.
     *
     * @param node
     *            to connect to.
     * @return The old start/end node being replaced by the new node, or {@code null} if it couldn't be connected.
     */
    public Node connectToNode(final Node node) {
        if (this.startNode.getPosition().distanceToSqr(node.getPosition())
                < this.endNode.getPosition().distanceToSqr(node.getPosition())) {
            if (this.startNode.degree() != 1) {
                return null;
            }
            final Node oldNode = this.startNode;
            this.points.add(0, node.getPosition());
            this.startNode = node;
            this.bounds.add(node.getPosition());
            return oldNode;
        } else {
            if (this.endNode.degree() != 1) {
                return null;
            }
            final Node oldNode = this.endNode;
            this.points.add(node.getPosition());
            this.endNode = node;
            this.bounds.add(node.getPosition());
            return oldNode;
        }
    }

    /**
     * Moves a point to another position.
     *
     * @param index
     *            of the point to be moved.
     * @param newPosition
     *            to move it to.
     * @return Whether moving succeeded.
     */
    public boolean movePoint(final int index, final Vec3 newPosition) {
        if (0 <= index && index < this.points.size()) {
            this.points.set(index, newPosition);
            this.bounds = new AxisAlignedBoundingBox(this.points.toArray(new Vec3[0]));
            this.updateLength();
            return true;
        }
        return false;
    }

    /**
     * Updates the length of the path.
     */
    public void updateLength() {
        double sum = 0;
        for (int i = 0; i < this.points.size() - 1; i++) {
            sum += this.points.get(i).distanceTo(this.points.get(i + 1));
        }
        this.length = sum;
    }

    /**
     * Gets the length of the path.
     *
     * @return The length.
     */
    public double getLength() {
        return this.length;
    }

    /**
     * Gets the start node.
     *
     * @return The start node.
     */
    public Node getStart() {
        return this.startNode;
    }

    /**
     * Gets the end node.
     *
     * @return The end node.
     */
    public Node getEnd() {
        return this.endNode;
    }

    /**
     * Gets all points of the path (nodes included).
     *
     * @return Points of the path.
     */
    public List<Vec3> getPoints() {
        return this.points;
    }

    @Override
    public AxisAlignedBoundingBox getBounds() {
        return this.bounds;
    }

    /**
     * Returns a reversed version of the path. All points and start/end node are swapped in their order.
     *
     * @return The reversed path.
     */
    public Path getReversed() {
        return this.reversed == null ? this.reversed = new ReversePath(this) : this.reversed;
    }

    /**
     * Splits this path into two.
     *
     * @param position
     *            where to split the path.
     * @param insertNewNode
     *            toggles inserting the given point as node, or just removing a segment.
     * @return The newly created paths, or {@code null} if splitting failed.
     */
    public List<Path> split(final Vec3 position, final boolean insertNewNode) {
        final int splitIndex = this.getNearestPointOnPath(position).a;
        Path left;
        Path right;
        if (insertNewNode) {
            final Node splitNode = new Node(position);
            left = splitIndex == 0
                    ? new Path(this.startNode, splitNode)
                    : new Path(this.startNode, this.points.subList(1, splitIndex + 1), splitNode);
            right = splitIndex >= this.points.size() - 2
                    ? new Path(splitNode, this.endNode)
                    : new Path(splitNode, this.points.subList(splitIndex + 1, this.points.size() - 1), this.endNode);
            left.relinkToNodes();
            right.relinkToNodes();
        } else {
            if (splitIndex < 1 || splitIndex > this.points.size() - 3) {
                return null;
            }
            final Node splitNodeLeft = new Node(this.points.get(splitIndex));
            final Node splitNodeRight = new Node(this.points.get(splitIndex + 1));
            left = new Path(this.startNode, this.points.subList(1, splitIndex), splitNodeLeft);
            right = new Path(splitNodeRight, this.points.subList(splitIndex + 2, this.points.size() - 1), this.endNode);
            left.relinkToNodes();
            right.relinkToNodes();
        }
        this.unlinkFromNodes();
        return Arrays.asList(left, right);
    }

    /**
     * Remove references to this path from its start/end nodes.
     */
    public void unlinkFromNodes() {
        this.startNode.connectedPaths.remove(this);
        this.endNode.connectedPaths.remove(this);
    }

    /**
     * Add references to this path to its start/end nodes.
     */
    public void relinkToNodes() {
        if (!this.startNode.connectedPaths.contains(this)) {
            this.startNode.connectedPaths.add(this);
        }
        if (!this.endNode.connectedPaths.contains(this)) {
            this.endNode.connectedPaths.add(this);
        }
    }

    /**
     * Finds the point in this path nearest to another given point.
     *
     * @param point
     *            to compare against.
     * @return A pair of the index of the closest point and the point itself.
     */
    public Pair<Integer, Vec3> getNearestPathPoint(final Vec3 point) {
        int nearest = -1;
        double dist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < this.points.size(); i++) {
            final double d = point.distanceToSqr(this.points.get(i));
            if (d < dist) {
                nearest = i;
                dist = d;
            }
        }
        return new Pair<>(nearest, this.points.get(nearest));
    }

    /**
     * Projects the given point onto the path's line segments.
     *
     * @param point
     *            to be projected.
     * @return A pair of the index of the line segment and the resulting point of the projection.
     */
    public Pair<Integer, Vec3> getNearestPointOnPath(final Vec3 point) {
        double minDist = Double.POSITIVE_INFINITY;
        int index = -1; // required initialisation - won't be returned, because paths always have at least two points
        Vec3 pathPoint = null;
        for (int i = 0; i < this.points.size() - 1; i++) {
            final Vec3 start = this.points.get(i);
            final Vec3 end = this.points.get(i + 1);
            final Vec3 dir = end.subtract(start);
            // use approximation via point-to-line-distance (true spline distance is overkill here)
            /*-
             * Start from:
             * (start + t * dir - point) * dir = 0
             * Vectors "point to point-on-line" and "dir" are perpendicular.
             * Expand and re-arrange:
             * start*dir + t*dir*dir - point*dir = 0
             * t*dir*dir = point*dir - start*dir
             * t = (point*dir - start*dir) / (dir*dir)
             */
            final double t = (point.dot(dir) - start.dot(dir)) / dir.dot(dir);
            if (t < 0) {
                final double dist = start.distanceToSqr(point);
                if (dist < minDist) {
                    minDist = dist;
                    index = i;
                    pathPoint = start;
                }
            } else if (t > 1) {
                final double dist = end.distanceToSqr(point);
                if (dist < minDist) {
                    minDist = dist;
                    index = i + 1;
                    pathPoint = end;
                }
            } else {
                final Vec3 linePoint = start.add(end.subtract(start).scale(t));
                final double dist = linePoint.distanceToSqr(point);
                if (dist < minDist) {
                    minDist = dist;
                    index = i;
                    pathPoint = linePoint;
                }
            }
        }
        return new Pair<>(index, pathPoint);
    }

    @Override
    public double squareDistance(final Vec3 point) {
        return point.distanceToSqr(this.getNearestPointOnPath(point).b);
    }

    /**
     * Calculates the distance from a given point to this path.
     *
     * @param point
     *            to get the distance of.
     * @return The distance to the given point.
     */
    public double getDistance(final Vec3 point) {
        return Math.sqrt(this.squareDistance(point));
    }

    /**
     * Wrapper to reverse a paths' direction.
     *
     * @author Kepler-17c
     */
    private static class ReversePath extends Path {
        /**
         * The original path.
         */
        private final Path source;

        /**
         * Wraps a normal path.
         *
         * @param source
         *            to wrap.
         */
        public ReversePath(final Path source) {
            super(source);
            this.source = source;
        }

        @Override
        public boolean appendPoint(final Vec3 point) {
            return super.prependPoint(point);
        }

        @Override
        public boolean prependPoint(final Vec3 point) {
            return super.appendPoint(point);
        }

        @Override
        public Node getStart() {
            return super.getEnd();
        }

        @Override
        public Node getEnd() {
            return super.getStart();
        }

        @Override
        public List<Vec3> getPoints() {
            final int size = super.points.size();
            return IntStream.rangeClosed(1, size)
                    .map(i -> size - i)
                    .mapToObj(super.points::get)
                    .collect(Collectors.toList());
        }

        @Override
        public Path getReversed() {
            return this.source;
        }
    }
}
