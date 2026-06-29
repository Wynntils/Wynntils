/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.abilitytree.parser.AbilityTreeParser;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.utils.mc.McUtils;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AbilityTreeModel extends Model {
    public static final int ABILITY_TREE_PAGES = 9;
    public static final AbilityTreeParser ABILITY_TREE_PARSER = new AbilityTreeParser();
    public static final AbilityTreeContainerQueries ABILITY_TREE_CONTAINER_QUERIES = new AbilityTreeContainerQueries();

    @Persisted
    private final Storage<Map<String, AbilityTreeInfo>> abilityTreeLoadouts = new Storage<>(new TreeMap<>());

    private Map<ClassType, AbilityTreeInfo> abilityTreeMap = new HashMap<>();
    private ParsedAbilityTree currentAbilityTree;

    public AbilityTreeModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_ABILITIES).handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, AbilityTreeInfo>>() {}.getType();
            Gson gson = new GsonBuilder().create();

            Map<String, AbilityTreeInfo> abilityMap = gson.fromJson(reader, type);

            Map<ClassType, AbilityTreeInfo> tempMap = new HashMap<>();

            abilityMap.forEach((key, value) -> tempMap.put(ClassType.fromName(key), value));

            abilityTreeMap = tempMap;
        });
    }

    public void setCurrentAbilityTree(ParsedAbilityTree currentAbilityTree) {
        this.currentAbilityTree = currentAbilityTree;
    }

    public void saveCurrentAbilityTree(String name) {
        ABILITY_TREE_CONTAINER_QUERIES.getUnlockedAbilityTree(treeInfo -> {
            abilityTreeLoadouts.get().put(name, treeInfo);
            WynntilsMod.info("Saved ability tree loadout: " + name);
        });
    }

    public void loadAbilityTree(String name, Consumer<String> onError, Runnable onComplete) {
        AbilityTreeInfo savedTree = abilityTreeLoadouts.get().get(name);
        if (savedTree == null) {
            onError.accept("No saved ability tree loadout: " + name);
            return;
        }

        List<AbilityTreeSkillNode> ordered = getIdealApplicationOrder(savedTree);
        WynntilsMod.info("ordered: " +  ordered);
        if (ordered.isEmpty()) {
            WynntilsMod.info("Loadout " + name + " is empty, nothing to apply");
            onComplete.run();
            return;
        }

        WynntilsMod.info("order: " + ordered);

        McUtils.player().closeContainer();

        // Wait for the container to close
        Managers.TickScheduler.scheduleLater(() ->
        ABILITY_TREE_CONTAINER_QUERIES.executeUnlocks(ordered, onError, onComplete), 50);
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

    public AbilityTreeInfo getAbilityTree(ClassType type) {
        return abilityTreeMap.get(type);
    }

    private List<AbilityTreeSkillNode> getIdealApplicationOrder(AbilityTreeInfo treeInfo) {
        List<AbilityTreeSkillNode> nodes = new ArrayList<>(treeInfo.nodes());
        if (nodes.isEmpty()) return List.of();

        Map<String, AbilityTreeSkillNode> byName = nodes.stream()
                .collect(Collectors.toMap(
                        AbilityTreeSkillNode::name,
                        n -> n,
                        (a, b) -> a
                ));

        Map<Integer, AbilityTreeSkillNode> byId = nodes.stream()
                .collect(Collectors.toMap(
                        AbilityTreeSkillNode::id,
                        n -> n
                ));

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
                .collect(Collectors.groupingBy(
                        AbilityTreeSkillNode::requiredAbility,
                        Collectors.counting()
                ));

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
                throw new IllegalStateException(
                        "Deadlock: no valid order for remaining nodes: " + remaining);
            }

            Map<AbilityTreeSkillNode, Integer> distances =
                    computeGraphDistances(unlocked, adjacency, byId);

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
            int currentPage,
            Map<String, Long> dependents,
            Map<AbilityTreeSkillNode, Integer> distances) {

        return Comparator
                // 1. Prefer nodes closest in the connection graph
                .comparingInt((AbilityTreeSkillNode n) ->
                        distances.getOrDefault(n, Integer.MAX_VALUE))

                // 2. Minimise UI page navigation
                .thenComparingInt((AbilityTreeSkillNode n) ->
                        pageDistance(currentPage, n.location().page()))

                // 3. Unlock prerequisites for the most downstream nodes first
                .thenComparing(
                        (AbilityTreeSkillNode n) -> dependents.getOrDefault(n.name(), 0L),
                        Comparator.reverseOrder())

                // 4. Prefer archetype contributors to hit thresholds sooner
                .thenComparing((AbilityTreeSkillNode n) -> n.archetype() != null, Comparator.reverseOrder())

                // 5. Stable, deterministic tie-breaker
                .thenComparingInt(AbilityTreeSkillNode::id);
    }

    private static int pageDistance(int from, int to) {
        if (to == from) return 0;                           // Same page
        if (to > from) return to - from;                    // Forward
        return 1000 + (from - to);         // Backward
    }
}
