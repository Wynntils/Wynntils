/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.CoreComponent;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public final class DownloadDependencyGraph {
    private final Map<Node, NodeState> nodeMap = new ConcurrentHashMap<>();

    private DownloadDependencyGraph(List<Node> nodes) {
        nodes.forEach(node ->
                nodeMap.put(node, node.dependencies.isEmpty() ? NodeState.QUEUED : NodeState.WAITING_ON_DEPENDENCY));
    }

    public static DownloadDependencyGraph build(List<QueuedDownload> downloads) {
        // Short circuit on no downloads, to handle the edge-case one and for all in all sanity checks
        if (downloads.isEmpty()) return new DownloadDependencyGraph(List.of());

        // Firstly, run sanity checks on the queued downloads to determine if a valid graph can be built
        // 1. Check for duplicate downloads
        checkDuplicateDownloads(downloads);

        // 2. Check for missing dependencies
        checkMissingDependencies(downloads);

        // Now, let's build a directed graph of the download dependencies
        List<Node> nodes = downloads.stream().map(Node::new).toList();
        nodes.forEach(node -> node.calculateDependencies(nodes));

        // The latter part of the sanity checks can be ran after building the graph:
        // 1. Check for circular dependencies
        checkCircularDependencies(nodes);

        return new DownloadDependencyGraph(nodes);
    }

    // region Processing

    public synchronized QueuedDownload nextDownload() {
        Node nextNode = nodeMap.entrySet().stream()
                .filter(entry -> entry.getValue() == NodeState.QUEUED)
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);

        if (nextNode == null) return null;

        nodeMap.put(nextNode, NodeState.IN_PROGRESS);

        return nextNode.download;
    }

    public void markDownloadCompleted(QueuedDownload download) {
        Node node = nodeMap.keySet().stream()
                .filter(n -> n.download == download)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Download not found in graph: " + download.urlId()));

        nodeMap.put(node, NodeState.COMPLETED);

        // Mark all dependents as ready to be downloaded, if all their dependencies are completed
        node.dependents.forEach(dependent -> {
            if (dependent.dependencies.stream()
                    .allMatch(dependency -> nodeMap.get(dependency) == NodeState.COMPLETED)) {
                nodeMap.put(dependent, NodeState.QUEUED);
            }
        });
    }

    public void markDownloadError(QueuedDownload download) {
        Node node = nodeMap.keySet().stream()
                .filter(n -> n.download == download)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Download not found in graph: " + download.urlId()));

        nodeMap.put(node, NodeState.ERROR);

        // Mark all dependents as error, as they cannot be downloaded
        node.dependents.forEach(dependent -> nodeMap.put(dependent, NodeState.ERROR));
    }

    public void markDownloadRetry(QueuedDownload download) {
        Node node = nodeMap.keySet().stream()
                .filter(n -> n.download == download)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Download not found in graph: " + download.urlId()));

        // Also mark all dependents as waiting on dependency, as they need to wait for the retry to complete
        node.dependents.forEach(dependent -> nodeMap.put(dependent, NodeState.WAITING_ON_DEPENDENCY));

        // And mark all dependencies as queued, as they need to be re-downloaded
        // Do this by traversing the graph from the node, and marking all dependencies as waiting on dependency
        // or queued, if they have no dependencies
        Deque<Node> stack = new LinkedList<>();
        stack.addFirst(node);

        while (!stack.isEmpty()) {
            Node currentNode = stack.pop();

            if (currentNode.dependencies.isEmpty()) {
                nodeMap.put(currentNode, NodeState.QUEUED);
            } else {
                nodeMap.put(currentNode, NodeState.WAITING_ON_DEPENDENCY);
                currentNode.dependencies.forEach(stack::addFirst);
            }
        }

        // Either mark the node as queued, or waiting on dependency if it has dependencies
        nodeMap.put(node, node.dependencies.isEmpty() ? NodeState.QUEUED : NodeState.WAITING_ON_DEPENDENCY);
    }

    public void resetState() {
        nodeMap.replaceAll(
                (node, state) -> node.dependencies.isEmpty() ? NodeState.QUEUED : NodeState.WAITING_ON_DEPENDENCY);
    }

    // endregion

    // region State and Progress

    public DownloadDependencyGraphState state() {
        return new DownloadDependencyGraphState(
                isFinished(), hasError(), totalDownloads(), successfulDownloads(), failedDownloads(), errorRate());
    }

    public boolean isFinished() {
        return nodeMap.values().stream().allMatch(state -> state == NodeState.COMPLETED || state == NodeState.ERROR);
    }

    public boolean hasError() {
        return nodeMap.values().stream().anyMatch(state -> state == NodeState.ERROR);
    }

    public int totalDownloads() {
        return nodeMap.size();
    }

    public int successfulDownloads() {
        return (int) nodeMap.values().stream()
                .filter(state -> state == NodeState.COMPLETED)
                .count();
    }

    public int failedDownloads() {
        return (int) nodeMap.values().stream()
                .filter(state -> state == NodeState.ERROR)
                .count();
    }

    public float errorRate() {
        return (float) failedDownloads() / totalDownloads();
    }

    public NodeState getDownloadState(QueuedDownload download) {
        Node node = nodeMap.keySet().stream()
                .filter(n -> n.download == download)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Download not found in graph: " + download.urlId()));

        return nodeMap.get(node);
    }

    // endregion

    // region Checks
    private static void checkDuplicateDownloads(List<QueuedDownload> downloads) {
        // Downloading the same resource in the same core component should always point to a schematic error
        // in the component itself, so mark it as a critical issue and crash the mod
        QueuedDownload duplicatedInSameComponent = downloads.stream()
                .filter(download -> downloads.stream().anyMatch(other -> download != other && download.equals(other)))
                .findAny()
                .orElse(null);

        if (duplicatedInSameComponent != null) {
            throw new IllegalStateException(duplicatedInSameComponent.callerComponent()
                    + " downloaded the same data twice: " + duplicatedInSameComponent.urlId());
        }

        // Downloading the same resource in different core components is not a direct confirmation of a schematic issue,
        // but is likely to be a mistake. Mark it as a warning and continue with the graph building
        List<QueuedDownload> duplicatedDownloadsInSeparateComponents = downloads.stream()
                .filter(download ->
                        downloads.stream().anyMatch(other -> download != other && download.urlId() == other.urlId()))
                .toList();

        if (!duplicatedDownloadsInSeparateComponents.isEmpty()) {
            WynntilsMod.warn("Multiple components downloaded the same data: "
                    + duplicatedDownloadsInSeparateComponents.getFirst().urlId() + ". Components: "
                    + StringUtils.join(duplicatedDownloadsInSeparateComponents.stream()
                            .map(QueuedDownload::callerComponent)
                            .toList()));
        }
    }

    private static void checkMissingDependencies(List<QueuedDownload> downloads) {
        Set<Pair<CoreComponent, UrlId>> allDependencies = downloads.stream()
                .flatMap(download -> download.dependency().dependencies().stream())
                .collect(Collectors.toUnmodifiableSet());

        for (Pair<CoreComponent, UrlId> dependency : allDependencies) {
            boolean missingDependency = downloads.stream()
                    .noneMatch(download ->
                            download.callerComponent() == dependency.a() && download.urlId() == dependency.b());

            if (missingDependency) {
                throw new IllegalStateException("Missing dependency: " + dependency.b() + " for " + dependency.a());
            }
        }
    }

    private static void checkCircularDependencies(List<Node> nodes) {
        for (Node node : nodes) {
            Deque<Node> stack = new LinkedList<>();
            stack.addFirst(node);

            while (!stack.isEmpty()) {
                Node currentNode = stack.pop();
                boolean circularDependency = currentNode.dependencies.stream()
                        .anyMatch(dependencyNode -> dependencyNode.dependencies.contains(node));

                if (circularDependency) {
                    throw new IllegalStateException("Circular dependency detected on: " + node.download.urlId()
                            + ". Starting from: " + node.download.callerComponent());
                }

                stack.addAll(currentNode.dependencies);
            }
        }
    }

    // endregion

    // region Debug

    void logGraph() {
        WynntilsMod.info("[DownloadManager] Download Dependency Graph:");

        // First, collect all UrlIds by their caller components
        Map<CoreComponent, List<UrlId>> urlIdsByComponent = nodeMap.keySet().stream()
                .sorted(Comparator.comparing(n -> n.download.callerComponent().getJsonName()))
                .collect(Collectors.groupingBy(
                        node -> node.download.callerComponent(),
                        Collectors.mapping(node -> node.download.urlId(), Collectors.toList())));

        // Then, log the graph, printing the caller component and its downloaded UrlIds, and their dependencies
        urlIdsByComponent.forEach((component, urlIds) -> {
            WynntilsMod.info("| -- " + StringUtils.capitalize(component.getJsonName()));
            urlIds.forEach(urlId -> {
                Node node = nodeMap.keySet().stream()
                        .filter(n -> n.download.callerComponent() == component && n.download.urlId() == urlId)
                        .findAny()
                        .orElseThrow();

                if (node.dependencies.isEmpty()) {
                    WynntilsMod.info("|    - " + urlId);
                } else {
                    WynntilsMod.info("|    - " + urlId + " <- "
                            + node.dependencies.stream()
                                    .map(dependency -> dependency.download.urlId())
                                    .toList());
                }
            });
        });
    }

    // endregion

    public record DownloadDependencyGraphState(
            boolean finished,
            boolean error,
            int totalDownloads,
            int successfulDownloads,
            int failedDownloads,
            float errorRate) {
        public boolean successful() {
            return !error && finished;
        }
    }

    public enum NodeState {
        WAITING_ON_DEPENDENCY,
        QUEUED,
        IN_PROGRESS,
        COMPLETED,
        ERROR,
    }

    private static final class Node {
        private final QueuedDownload download;

        private List<Node> dependencies = List.of();
        private List<Node> dependents = List.of();

        private Node(QueuedDownload download) {
            this.download = download;
        }

        private void calculateDependencies(List<Node> nodes) {
            List<Node> dependencies = new ArrayList<>();
            List<Node> dependents = new ArrayList<>();

            for (Node node : nodes) {
                if (download.dependency().dependsOn(node.download.callerComponent(), node.download.urlId())) {
                    dependencies.add(node);
                } else if (node.download.dependency().dependsOn(download.callerComponent(), download.urlId())) {
                    dependents.add(node);
                }
            }

            this.dependencies = List.copyOf(dependencies);
            this.dependents = List.copyOf(dependents);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(download, node.download);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(download);
        }
    }
}
