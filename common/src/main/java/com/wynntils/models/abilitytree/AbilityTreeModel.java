/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.abilitytree.parser.AbilityTreeParser;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AbilityTreeResetContainer;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import com.wynntils.models.items.items.gui.AbilityTreeNodeItem;
import com.wynntils.models.items.items.gui.AbilityTreeResetItem;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class AbilityTreeModel extends Model {
    public static final int ABILITY_TREE_PAGES = 9;
    public static final AbilityTreeParser ABILITY_TREE_PARSER = new AbilityTreeParser();
    public static final AbilityTreeContainerQueries ABILITY_TREE_CONTAINER_QUERIES = new AbilityTreeContainerQueries();
    private final AbilityTreeInfoRegistry abilityTreeInfoRegistry = new AbilityTreeInfoRegistry();

    @Persisted
    private final Storage<Map<String, List<String>>> unlockedAbilities = new Storage<>(new TreeMap<>());

    public AbilityTreeModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        abilityTreeInfoRegistry.registerDownloads(registry);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContentSet(ContainerSetContentEvent.Pre event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        List<Integer> abilitySlots = new ArrayList<>();
        if (currentContainer instanceof AbilityTreeContainer abilityTreeContainer) {
            abilitySlots =
                    new ArrayList<>(abilityTreeContainer.getAbilityBounds().getSlots());
        }

        if (abilitySlots.isEmpty()) return;

        Map<String, List<String>> allEquippedAbilities = unlockedAbilities.get();
        String characterId = Models.Character.getId();
        List<String> equipped = allEquippedAbilities.computeIfAbsent(characterId, id -> new ArrayList<>());

        boolean changed = false;

        for (int i = 0; i < event.getItems().size(); i++) {
            boolean slotInsidePossibleSlots = abilitySlots.contains(i);

            if (!slotInsidePossibleSlots) continue;

            ItemStack itemStack = event.getItems().get(i);
            if (itemStack.isEmpty()) continue;

            Optional<AbilityTreeNodeItem> abilityItemOpt = Models.Item.asWynnItem(itemStack, AbilityTreeNodeItem.class);
            if (abilityItemOpt.isEmpty()) continue;

            AbilityTreeNodeItem abilityItem = abilityItemOpt.get();
            String abilityName = abilityItem.getName().getString(StyleType.NONE);
            boolean unlocked = abilityItem.getAbilityTreeNodeType().getState() == AbilityTreeNodeState.UNLOCKED;

            if (unlocked) {
                if (!equipped.contains(abilityName)) {
                    equipped.add(abilityName);
                    changed = true;
                }
            } else {
                if (equipped.remove(abilityName)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            allEquippedAbilities.put(characterId, equipped);
            unlockedAbilities.store(allEquippedAbilities);
            unlockedAbilities.touched();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        List<Integer> abilitySlots = new ArrayList<>();
        if (currentContainer instanceof AbilityTreeContainer abilityTreeContainer) {
            abilitySlots =
                    new ArrayList<>(abilityTreeContainer.getAbilityBounds().getSlots());
        }

        if (abilitySlots.isEmpty()) return;

        int changedSlot = event.getSlot();
        boolean slotInsidePossibleSlots = abilitySlots.contains(changedSlot);

        if (!slotInsidePossibleSlots) return;

        ItemStack changedItemStack = event.getItemStack();
        if (changedItemStack.isEmpty()) return;

        Optional<AbilityTreeNodeItem> abilityItemOpt =
                Models.Item.asWynnItem(changedItemStack, AbilityTreeNodeItem.class);
        if (abilityItemOpt.isEmpty()) return;

        AbilityTreeNodeItem abilityItem = abilityItemOpt.get();
        String abilityName = abilityItem.getName().getString(StyleType.NONE);
        boolean unlocked = abilityItem.getAbilityTreeNodeType().getState() == AbilityTreeNodeState.UNLOCKED;

        Map<String, List<String>> allEquippedAbilities = unlockedAbilities.get();
        String characterId = Models.Character.getId();
        List<String> equipped = allEquippedAbilities.computeIfAbsent(characterId, id -> new ArrayList<>());

        boolean changed = false;

        if (unlocked) {
            if (!equipped.contains(abilityName)) {
                equipped.add(abilityName);
                changed = true;
            }
        } else {
            if (equipped.remove(abilityName)) {
                changed = true;
            }
        }

        if (changed) {
            allEquippedAbilities.put(characterId, equipped);
            unlockedAbilities.store(allEquippedAbilities);
            unlockedAbilities.touched();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void handleAbilityTreeResetClick(ContainerClickEvent event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        if (currentContainer instanceof AbilityTreeContainer) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.isEmpty()) return;

            Optional<AbilityTreeItem> abilityTreeItemOpt = Models.Item.asWynnItem(itemStack, AbilityTreeItem.class);
            if (abilityTreeItemOpt.isEmpty()) return;
            if (!abilityTreeItemOpt.get().getCanReset()) return;

            Map<String, List<String>> allEquippedAbilities = unlockedAbilities.get();
            allEquippedAbilities.put(Models.Character.getId(), new ArrayList<>());
            unlockedAbilities.store(allEquippedAbilities);
            unlockedAbilities.touched();
        } else if (currentContainer instanceof AbilityTreeResetContainer) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.isEmpty()) return;

            Optional<AbilityTreeResetItem> abilityResetItemOpt =
                    Models.Item.asWynnItem(itemStack, AbilityTreeResetItem.class);
            if (abilityResetItemOpt.isEmpty()) return;
            if (!abilityResetItemOpt.get().getCanReset()) return;

            if (event.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                Map<String, List<String>> allEquippedAbilities = unlockedAbilities.get();
                allEquippedAbilities.put(Models.Character.getId(), new ArrayList<>());
                unlockedAbilities.store(allEquippedAbilities);
                unlockedAbilities.touched();
            }
        }

        return;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void handleAbilityTreeEditClick(ContainerClickEvent event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        if (!(currentContainer instanceof AbilityTreeContainer)) return;

        ItemStack itemStack = event.getItemStack();
        if (itemStack.isEmpty()) return;

        Optional<AbilityTreeNodeItem> abilityItemOpt = Models.Item.asWynnItem(itemStack, AbilityTreeNodeItem.class);
        if (abilityItemOpt.isEmpty()) return;

        StatusEffect statusEffect = Models.StatusEffect.searchStatusEffectByName("Tree Manipulation");

        if (statusEffect == null || event.getMouseButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

        AbilityTreeNodeItem abilityItem = abilityItemOpt.get();
        String abilityName = abilityItem.getName().getString(StyleType.NONE);
        AbilityTreeSkillNode abilityTreeSkillNode =
                getNodeFromNameAndClass(abilityName, Models.Character.getClassType());

        if (abilityTreeSkillNode == null) return;

        Set<AbilityTreeSkillNode> toRemove =
                computeCascadeRemoval(abilityTreeSkillNode, Models.Character.getClassType());

        Map<String, List<String>> allEquippedAbilities = unlockedAbilities.get();
        String characterId = Models.Character.getId();
        List<String> equipped = allEquippedAbilities.computeIfAbsent(characterId, id -> new ArrayList<>());

        for (AbilityTreeSkillNode node : toRemove) {
            equipped.remove(node.name());
        }

        allEquippedAbilities.put(characterId, equipped);
        unlockedAbilities.store(allEquippedAbilities);
        unlockedAbilities.touched();
    }

    public List<String> getUnlockedAbilities() {
        return unlockedAbilities.get().getOrDefault(Models.Character.getId(), new ArrayList<>());
    }

    public Optional<String> getUnlockedAbilityByName(String abilityName) {
        List<String> characterAbilities =
                unlockedAbilities.get().getOrDefault(Models.Character.getId(), new ArrayList<>());

        return characterAbilities.stream()
                .filter(aspect -> aspect.toLowerCase(Locale.ROOT).endsWith(abilityName.toLowerCase(Locale.ROOT)))
                .findFirst();
    }

    public void clearUnlockedAbilitesAndRescan(
            Consumer<String> onStatus, Consumer<String> onError, Consumer<String> onComplete) {
        Map<String, List<String>> allEquippedAbilities = unlockedAbilities.get();
        allEquippedAbilities.put(Models.Character.getId(), new ArrayList<>());
        unlockedAbilities.store(allEquippedAbilities);
        unlockedAbilities.touched();

        McUtils.player().closeContainer();

        Managers.TickScheduler.scheduleNextTick(() -> Models.AbilityTree.ABILITY_TREE_CONTAINER_QUERIES.dumpAbilityTree(
                abilityTreeInfo -> {}, // we don't need to do anything with this because the container event reads it.
                onStatus,
                onError,
                onComplete));
    }

    public AbilityTreeInfo getAbilityTree(ClassType type) {
        return abilityTreeInfoRegistry.getAbilityTree(type);
    }

    public AbilityTreeSkillNode getNodeFromNameAndClass(String name, ClassType classType) {
        return abilityTreeInfoRegistry.getNodeFromNameAndClass(name, classType);
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
        SavableAbilityTree savedTree = Services.loadout.getAbilityTreeLoadout(name);
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

            if (next.archetypeInfo() != null) {
                archetypePoints.merge(next.archetypeInfo().archetype(), 1, Integer::sum);
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

                // 4. Prefer archetypeInfo.archetype() contributors to hit thresholds sooner
                .thenComparing(
                        (AbilityTreeSkillNode n) ->
                                n.archetypeInfo() != null && n.archetypeInfo().archetype() != null,
                        Comparator.reverseOrder())

                // 5. Stable, deterministic tie-breaker
                .thenComparingInt(AbilityTreeSkillNode::id);
    }

    private static int pageDistance(int from, int to) {
        if (to == from) return 0; // Same page
        if (to > from) return to - from; // Forward
        return 1000 + (from - to); // Backward
    }

    private Set<AbilityTreeSkillNode> computeCascadeRemoval(AbilityTreeSkillNode clickedNode, ClassType classType) {
        AbilityTreeInfo treeInfo = getAbilityTree(classType);
        List<AbilityTreeSkillNode> allNodes = treeInfo.nodes();

        Map<String, AbilityTreeSkillNode> byName =
                allNodes.stream().collect(Collectors.toMap(AbilityTreeSkillNode::name, n -> n, (a, b) -> a));

        String characterId = Models.Character.getId();
        List<String> equippedNames = unlockedAbilities.get().getOrDefault(characterId, List.of());

        // Full set of nodes the player had before the removal (includes the clicked one)
        Set<AbilityTreeSkillNode> preEquipped = equippedNames.stream()
                .map(byName::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        // For each node, record its original parents (nodes that had this node's id in connections)
        // that were also equipped pre‑removal.
        Map<AbilityTreeSkillNode, Set<AbilityTreeSkillNode>> preParents = new HashMap<>();
        for (AbilityTreeSkillNode node : preEquipped) {
            Set<AbilityTreeSkillNode> parents = allNodes.stream()
                    .filter(p -> p.connections().contains(node.id()) && preEquipped.contains(p))
                    .collect(Collectors.toSet());
            preParents.put(node, parents);
        }

        // Start with the clicked node as removed
        Set<AbilityTreeSkillNode> removed = new HashSet<>();
        removed.add(clickedNode);
        Set<AbilityTreeSkillNode> remaining = new HashSet<>(preEquipped);
        remaining.remove(clickedNode);

        boolean changed;
        do {
            changed = false;
            Iterator<AbilityTreeSkillNode> it = remaining.iterator();
            while (it.hasNext()) {
                AbilityTreeSkillNode node = it.next();

                // 1. Explicit ability requirement no longer satisfied
                if (node.requiredAbility() != null
                        && removed.stream().anyMatch(r -> r.name().equals(node.requiredAbility()))) {
                    it.remove();
                    removed.add(node);
                    changed = true;
                    continue;
                }

                // 2. Archetype requirement no longer met
                if (node.requiredArchetype() != null) {
                    // Count archetype points from the *remaining* set, excluding the node itself
                    Map<String, Integer> archetypeCounts = new HashMap<>();
                    for (AbilityTreeSkillNode n : remaining) {
                        if (n != node
                                && n.archetypeInfo() != null
                                && n.archetypeInfo().archetype() != null) {
                            archetypeCounts.merge(n.archetypeInfo().archetype(), 1, Integer::sum);
                        }
                    }
                    String reqArch = node.requiredArchetype().name();
                    int needed = node.requiredArchetype().required();
                    int have = archetypeCounts.getOrDefault(reqArch, 0);
                    if (have < needed) {
                        it.remove();
                        removed.add(node);
                        changed = true;
                        continue;
                    }
                }

                // 3. Connectivity: only remove if it *had* a parent before and now has none
                Set<AbilityTreeSkillNode> origParents = preParents.get(node);
                if (origParents != null && !origParents.isEmpty()) {
                    boolean parentRemains = origParents.stream().anyMatch(remaining::contains);
                    if (!parentRemains) {
                        it.remove();
                        removed.add(node);
                        changed = true;
                    }
                }
            }
        } while (changed);

        return removed;
    }
}
