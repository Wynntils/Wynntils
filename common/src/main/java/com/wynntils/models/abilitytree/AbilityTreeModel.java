/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.abilitytree.parser.AbilityTreeParser;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class AbilityTreeModel extends Model {
    public static final int ABILITY_TREE_PAGES = 9;
    public static final AbilityTreeParser ABILITY_TREE_PARSER = new AbilityTreeParser();
    public static final AbilityTreeContainerQueries ABILITY_TREE_CONTAINER_QUERIES = new AbilityTreeContainerQueries();
    private final AbilityTreeInfoRegistry abilityTreeInfoRegistry = new AbilityTreeInfoRegistry();

    private ParsedAbilityTree currentAbilityTree;

    public AbilityTreeModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        abilityTreeInfoRegistry.registerDownloads(registry);
    }

    public AbilityTreeInfo getAbilityTree(ClassType type) {
        return abilityTreeInfoRegistry.getAbilityTree(type);
    }

    public AbilityTreeSkillNode getNodeFromNameAndClass(String name, ClassType classType) {
        return abilityTreeInfoRegistry.getNodeFromNameAndClass(name, classType);
    }

    public void setCurrentAbilityTree(ParsedAbilityTree currentAbilityTree) {
        this.currentAbilityTree = currentAbilityTree;
    }

    public void saveCurrentAbilityTree(
            String name, Consumer<String> onStatus, Consumer<String> onError, Consumer<String> onComplete) {
        ABILITY_TREE_CONTAINER_QUERIES.getUnlockedAbilityTree(
                treeInfo -> {
                    List<String> abilityNames = treeInfo.nodes().stream()
                            .map(AbilityTreeSkillNode::name)
                            .toList();
                    ClassType classType = Models.Character.getClassType();
                    Services.loadout.saveAbilityTreeLoadout(name, new SavableAbilityTree(abilityNames, classType));
                    WynntilsMod.info("Saved ability tree loadout: " + name);
                },
                onStatus,
                onError,
                onComplete);
    }

    public void loadAbilityTree(
            String name, Consumer<String> onStatus, Consumer<String> onError, Consumer<String> onComplete) {
        SavableAbilityTree savedTree =  Services.loadout.getAbilityTreeLoadout(name);
        if (savedTree == null) {
            onError.accept("No saved ability tree loadout: " + name);
            return;
        }

        List<AbilityTreeSkillNode> ordered = getIdealApplicationOrder(savedTree, onError);
        if (ordered == null) return;
        if (ordered.isEmpty()) {
            onComplete.accept("Loadout " + name + " is empty, nothing to apply");
            return;
        }

        ContainerUtils.closeBackgroundContainer();

        ABILITY_TREE_CONTAINER_QUERIES.applyAbilityTreeLoadout(ordered, onStatus, onError, onComplete);
    }

    public AbilityTreeNodeState getNodeState(AbilityTreeSkillNode node) {
        if (currentAbilityTree == null) {
            return AbilityTreeNodeState.LOCKED;
        }

        return currentAbilityTree.nodes().keySet().stream()
                .filter(n -> n.equals(node))
                .map(currentAbilityTree.nodes()::get)
                .findFirst()
                .orElse(AbilityTreeNodeState.LOCKED);
    }

    private List<AbilityTreeSkillNode> getIdealApplicationOrder(
            SavableAbilityTree savedTree, Consumer<String> onError) {
        List<AbilityTreeSkillNode> nodes = savedTree.abilities().stream()
                .map(abilityName -> getNodeFromNameAndClass(abilityName, savedTree.classType()))
                .filter(Objects::nonNull)
                .map(AbilityTreeSkillNode::withUnlockedType)
                .collect(Collectors.toCollection(ArrayList::new));
        if (nodes.isEmpty()) return List.of();

        Map<String, AbilityTreeSkillNode> byName =
                nodes.stream().collect(Collectors.toMap(AbilityTreeSkillNode::name, n -> n, (a, b) -> a));

        Map<Integer, AbilityTreeSkillNode> byId =
                nodes.stream().collect(Collectors.toMap(AbilityTreeSkillNode::id, n -> n));

        // Bidirectional adjacency from the directed connections list
        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        for (AbilityTreeSkillNode node : nodes) {
            adjacency.computeIfAbsent(node.id(), k -> new ArrayList<>());
            for (int conn : node.connections()) {
                adjacency.get(node.id()).add(conn);
                adjacency.computeIfAbsent(conn, k -> new ArrayList<>()).add(node.id());
            }
        }

        Map<String, Long> dependents = nodes.stream()
                .filter(n -> n.requiredAbility() != null)
                .collect(Collectors.groupingBy(AbilityTreeSkillNode::requiredAbility, Collectors.counting()));

        List<AbilityTreeSkillNode> order = new ArrayList<>();
        Set<AbilityTreeSkillNode> unlocked = new HashSet<>();
        Map<String, Integer> archetypePoints = new HashMap<>();
        List<AbilityTreeSkillNode> remaining = new ArrayList<>(nodes);

        int currentPage = 1;

        while (!remaining.isEmpty()) {
            List<AbilityTreeSkillNode> available = new ArrayList<>();
            for (AbilityTreeSkillNode node : remaining) {
                if (isAvailable(node, byName, unlocked, archetypePoints)) {
                    available.add(node);
                }
            }

            if (available.isEmpty()) {
                onError.accept("Failed to build ability application order. Probably because the ability tree updated.");
                WynntilsMod.error("no valid order for remaining nodes: " + remaining);
                return null;
            }

            Map<AbilityTreeSkillNode, Integer> distances = computeGraphDistances(unlocked, adjacency, byId);

            AbilityTreeSkillNode next = pickBest(available, currentPage, dependents, distances);
            order.add(next);
            unlocked.add(next);
            remaining.remove(next);
            currentPage = next.location().page();

            if (next.archetype() != null) {
                archetypePoints.merge(next.archetype(), 1, Integer::sum);
            }
        }

        return order;
    }

    private static Map<AbilityTreeSkillNode, Integer> computeGraphDistances(
            Set<AbilityTreeSkillNode> unlocked,
            Map<Integer, List<Integer>> adjacency,
            Map<Integer, AbilityTreeSkillNode> byId) {
        Map<AbilityTreeSkillNode, Integer> distances = new HashMap<>();
        ArrayDeque<AbilityTreeSkillNode> queue = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();

        for (AbilityTreeSkillNode start : unlocked) {
            queue.add(start);
            visited.add(start.id());
            distances.put(start, 0);
        }

        int distance = 0;
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                AbilityTreeSkillNode current = queue.poll();

                for (int neighborId : adjacency.getOrDefault(current.id(), List.of())) {
                    if (!visited.add(neighborId)) continue;

                    AbilityTreeSkillNode neighbor = byId.get(neighborId);
                    if (neighbor != null) {
                        distances.put(neighbor, distance + 1);
                        queue.add(neighbor);
                    }
                }
            }
            distance++;
        }

        return distances;
    }

    private static boolean isAvailable(
            AbilityTreeSkillNode node,
            Map<String, AbilityTreeSkillNode> byName,
            Set<AbilityTreeSkillNode> unlocked,
            Map<String, Integer> archetypePoints) {
        if (node.requiredAbility() != null) {
            AbilityTreeSkillNode required = byName.get(node.requiredAbility());
            if (required == null || !unlocked.contains(required)) {
                return false;
            }
        }

        if (node.requiredArchetype() != null) {
            int have = archetypePoints.getOrDefault(node.requiredArchetype().name(), 0);
            return have >= node.requiredArchetype().required();
        }

        return true;
    }

    private static AbilityTreeSkillNode pickBest(
            List<AbilityTreeSkillNode> available,
            int currentPage,
            Map<String, Long> dependents,
            Map<AbilityTreeSkillNode, Integer> distances) {
        return available.stream()
                .min(nodeComparator(currentPage, dependents, distances))
                .orElseThrow();
    }

    private static Comparator<AbilityTreeSkillNode> nodeComparator(
            int currentPage, Map<String, Long> dependents, Map<AbilityTreeSkillNode, Integer> distances) {
        return Comparator
                // 1. Prefer nodes closest in the connection graph
                .comparingInt((AbilityTreeSkillNode n) -> distances.getOrDefault(n, Integer.MAX_VALUE))

                // 2. Minimise UI page navigation
                .thenComparingInt((AbilityTreeSkillNode n) ->
                        pageDistance(currentPage, n.location().page()))

                // 3. Unlock prerequisites for the most downstream nodes first
                .thenComparing(
                        (AbilityTreeSkillNode n) -> dependents.getOrDefault(n.name(), 0L), Comparator.reverseOrder())

                // 4. Prefer archetype contributors to hit thresholds sooner
                .thenComparing((AbilityTreeSkillNode n) -> n.archetype() != null, Comparator.reverseOrder())

                // 5. Stable, deterministic tie-breaker
                .thenComparingInt(AbilityTreeSkillNode::id);
    }

    private static int pageDistance(int from, int to) {
        if (to == from) return 0; // Same page
        if (to > from) return to - from; // Forward
        return 1000 + (from - to); // Backward
    }
}
