/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.graph;

import com.wynntils.utils.objects.bvh.BoundingVolumeHierarchy;
import com.wynntils.utils.objects.minQueue.FibonacciHeapMinQueue;
import com.wynntils.wc.utils.lootrun.LootrunUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import net.minecraft.world.phys.Vec3;

/**
 * At its core a basic graph implementation. Added on top are a bounding-volume-hierarchy for efficient search of nodes
 * or vertices, and path-finding with Dijkstra's algorithm.
 *
 * @author Kepler-17c
 */
public class MapGraph {

    /**
     * Nodes of the graph. Used for quick spatial search.
     */
    protected final BoundingVolumeHierarchy<Node> nodeTree;
    /**
     * Vertices of the graph. Used for quick spatial search.
     */
    protected final BoundingVolumeHierarchy<Path> pathTree;

    /**
     * Create an empty graph or load an existing one. If {@code file} is {@code null}, this creates a new empty graph.
     */
    public MapGraph() {
        this.nodeTree = new BoundingVolumeHierarchy<>();
        this.pathTree = new BoundingVolumeHierarchy<>();
    }

    public LootrunUtils.Path findPath(final Vec3 from, final Vec3 to) {
        final Path startPath = this.pathTree.findNearest(from);
        final Path endPath = this.pathTree.findNearest(to);
        // insert temporary path splits to create start- & target-nodes for Dijkstra
        final List<Path> temporaryPaths = new ArrayList<>();
        Vec3 startPoint;
        Node startNode;
        final Vec3 endPoint;
        final Predicate<Node> endNode;
        if (startPath == endPath) { // split into 3 parts
            // find start-point and -node
            startPoint = startPath.getNearestPointOnPath(from).b;
            final List<Path> firstSplit = startPath.split(startPoint, true);
            temporaryPaths.addAll(firstSplit);
            startNode = firstSplit.get(0).getEnd();
            // find end-point and -node
            final double leftDist = firstSplit.get(0).squareDistance(to);
            final double rightDist = firstSplit.get(1).squareDistance(to);
            final Path endSegment = leftDist < rightDist ? firstSplit.get(0) : firstSplit.get(1);
            endPoint = endPath.getNearestPointOnPath(to).b;
            final List<Path> secondSplit = endSegment.split(endPoint, true);
            temporaryPaths.addAll(secondSplit);
            endNode = node -> node == secondSplit.get(0).getEnd();
        } else { // split each into 2 parts
            startPoint = startPath.getNearestPointOnPath(from).b;
            endPoint = endPath.getNearestPointOnPath(to).b;
            final List<Path> startSplit = startPath.split(startPoint, true);
            final List<Path> endSplit = endPath.split(endPoint, true);
            startNode = startSplit.get(0).getEnd();
            endNode = node -> node == endSplit.get(0).getEnd();
        }
        final List<Path> shortestPath = this.dijkstra(startNode, endNode);
        temporaryPaths.forEach(Path::unlinkFromNodes);
        startPath.relinkToNodes();
        endPath.relinkToNodes();
        if (shortestPath == null) {
            return null;
        }
        // combine result paths and return
        final LootrunUtils.Path result = new LootrunUtils.Path(new ArrayList<>());
        result.points().add(from);
        result.points().add(startPoint);
        shortestPath.forEach(segment -> {
            final List<Vec3> points = segment.getPoints();
            result.points().addAll(points.subList(1, points.size()));
        });
        result.points().add(to);
        return result;
    }

    /**
     * Use Dijkstra's algorithm to find the nearest node from {@code startNode} to satisfy the search criteria.
     *
     * @param startNode
     *            The node to start the search from.
     * @param isTargetNode
     *            A function of search-criteria, to match encountered nodes against.
     * @return The shortest path found to a node satisfying {@code isTargetNode}, or {@code null} if there is no such
     *         node in reach.
     */
    private List<Path> dijkstra(final Node startNode, final Predicate<Node> isTargetNode) {
        // prepare nodes
        final Map<Node, Double> weights = new HashMap<>();
        final Map<Node, Path> precedingPaths = new HashMap<>();
        // set up working queue
        final Comparator<Node> nodeComparator =
                Comparator.comparingDouble(node -> weights.getOrDefault(node, Double.POSITIVE_INFINITY));
        final Queue<Node> shortestDistanceNode = new FibonacciHeapMinQueue<>(nodeComparator);
        weights.put(startNode, 0.0);
        shortestDistanceNode.add(startNode);
        // actual algorithm
        Node activeNode = null;
        while (!shortestDistanceNode.isEmpty() && !isTargetNode.test(activeNode = shortestDistanceNode.poll())) {
            for (final Path p : activeNode.connectedPaths) {
                if (p.getStart() == activeNode || !p.isDirected()) {
                    final double distanceSum =
                            weights.getOrDefault(activeNode, Double.POSITIVE_INFINITY) + p.getLength();
                    final Node nextNode = p.getStart() == activeNode ? p.getEnd() : p.getStart();
                    if (distanceSum < weights.getOrDefault(nextNode, Double.POSITIVE_INFINITY)) {
                        shortestDistanceNode.remove(nextNode);
                        weights.put(nextNode, distanceSum);
                        precedingPaths.put(nextNode, p);
                        shortestDistanceNode.add(nextNode);
                    }
                }
            }
        }
        if (!isTargetNode.test(activeNode)) {
            return null; // a path does not exist for the given input
        }
        final List<Path> result = new LinkedList<>();
        while (activeNode != startNode) {
            final Path path = precedingPaths.get(activeNode).getEnd() == activeNode // if oriented correctly
                    ? precedingPaths.get(activeNode) // use it
                    : precedingPaths.get(activeNode).getReversed(); // else reverse
            result.add(0, path);
            activeNode = path.getStart();
        }
        return result;
    }
}
