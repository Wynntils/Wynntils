/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.google.gson.JsonElement;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * This class contains all relevant info to a specific class' ability tree.
 */
public class AbilityTreeInfo {
    private final List<AbilityTreeSkillNode> nodes = new ArrayList<>();

    // region Processing

    // Don't serialize these field, they are only used for processing
    private final transient Map<AbilityTreeLocation, AbilityTreeConnectionType> connectionMap = new HashMap<>();
    private final transient Map<AbilityTreeLocation, AbilityTreeSkillNode> nodeMap = new HashMap<>();

    public void addNodeFromItem(ItemStack itemStack, int page, int slot) {
        AbilityTreeSkillNode node = Models.AbilityTree.ABILITY_TREE_PARSER
                .parseNodeFromItem(itemStack, page, slot, nodes.size() + 1)
                .key();

        nodes.add(node);
        nodeMap.put(AbilityTreeLocation.fromSlot(slot, page), node);
    }

    public void addConnectionFromItem(ItemStack itemStack, int page, int slot) {
        connectionMap.put(
                AbilityTreeLocation.fromSlot(slot, page),
                AbilityTreeConnectionType.fromDamage(itemStack.getDamageValue()));
    }

    public void processItem(ItemStack itemStack, int page, int slot, boolean processConnections) {
        if (Models.AbilityTree.ABILITY_TREE_PARSER.isNodeItem(itemStack, slot)) {
            addNodeFromItem(itemStack, page, slot);
            return;
        }

        if (processConnections && Models.AbilityTree.ABILITY_TREE_PARSER.isConnectionItem(itemStack)) {
            addConnectionFromItem(itemStack, page, slot);
        }
    }

    public void processConnections() {
        AbilityTreeSkillNode firstNode = nodes.stream()
                .filter(node -> node.id() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No root node found!"));

        // Start at the connections of the root node
        Deque<AbilityTreeLocation> locationQueue = new LinkedList<>(getAdjacentConnections(firstNode.location(), null));

        // This set contains visited locations while processing a node so we don't go backwards
        Set<AbilityTreeLocation> visitedLocations = new HashSet<>();

        // This set contains nodes that have already been processed, so we don't process them again
        Set<AbilityTreeSkillNode> processedNodes = new HashSet<>();

        AbilityTreeSkillNode currentNode = firstNode;

        while (!locationQueue.isEmpty()) {
            AbilityTreeLocation currentLocation = locationQueue.poll();

            // Skip locations we have already visited while processing this node
            if (visitedLocations.contains(currentLocation)) continue;
            visitedLocations.add(currentLocation);

            AbilityTreeSkillNode nodeAtPosition = nodeMap.get(currentLocation);
            if (nodeAtPosition != null) {
                if (processedNodes.contains(nodeAtPosition)) continue;

                // At our current location there is a node, that is the new current node
                currentNode = nodeAtPosition;
                processedNodes.add(currentNode);
                visitedLocations.clear();
            }

            // We have to respect connection types
            // or use null if current location is a node and handle the logic separately
            AbilityTreeConnectionType connectionType = connectionMap.getOrDefault(currentLocation, null);

            List<AbilityTreeLocation> adjacentConnections = getAdjacentConnections(currentLocation, connectionType);

            // Add the adjacent connections to the front of the queue (dfs)
            adjacentConnections.stream()
                    .filter(location -> !locationQueue.contains(location))
                    .forEach(locationQueue::addFirst);

            // If connectionType is null, current location is a node, so there is no nodes next to it
            if (connectionType == null) continue;

            // Get adjacent nodes, but filter out current node and backwards connections
            final int currentNodeId = currentNode.id();
            List<AbilityTreeLocation> adjacentNodes = getAdjacentNodes(currentLocation, connectionType).stream()
                    .map(nodeMap::get)
                    .filter(node -> node.id() != currentNodeId)
                    .filter(node -> !node.connections().contains(currentNodeId))
                    .map(AbilityTreeSkillNode::location)
                    .toList();

            // Add the adjacent nodes to the back of the queue (bfs)
            adjacentNodes.stream()
                    .filter(location -> !locationQueue.contains(location))
                    .forEach(locationQueue::addLast);

            // Adjacent nodes are connected to the current node
            currentNode
                    .connections()
                    .addAll(adjacentNodes.stream()
                            .map(nodeMap::get)
                            .map(AbilityTreeSkillNode::id)
                            .toList());
        }
    }

    private List<AbilityTreeLocation> getAdjacentConnections(
            AbilityTreeLocation location, AbilityTreeConnectionType connectionType) {
        List<AbilityTreeLocation> locations = new ArrayList<>();

        // Find the adjacent connections in 3 directions: right, down, left
        AbilityTreeLocation[] adjacent = {location.right(), location.down(), location.left()};

        if (connectionType == null) {
            // Rely on checking neighboring connection types
            for (int i = 0; i < adjacent.length; i++) {
                AbilityTreeLocation adjacentLocation = adjacent[i];
                if (connectionMap.containsKey(adjacentLocation)) {
                    AbilityTreeConnectionType connection = connectionMap.get(adjacentLocation);
                    if (!connection.getPossibleDirections()[(i + 3) % 4]) continue;

                    locations.add(adjacentLocation);
                }
            }

            return locations;
        }

        boolean[] possibleDirections = connectionType.getPossibleDirections();

        for (int i = 0; i < adjacent.length; i++) {
            if (!possibleDirections[i + 1]) continue;

            AbilityTreeLocation adjacentLocation = adjacent[i];
            if (connectionMap.containsKey(adjacentLocation)) {
                locations.add(adjacentLocation);
            }
        }

        return locations;
    }

    private List<AbilityTreeLocation> getAdjacentNodes(
            AbilityTreeLocation location, AbilityTreeConnectionType connectionType) {
        List<AbilityTreeLocation> locations = new ArrayList<>();

        // Find the adjacent connections in 3 directions: right, down, left
        AbilityTreeLocation[] adjacent = {location.right(), location.down(), location.left()};
        boolean[] possibleDirections = connectionType.getPossibleDirections();

        for (int i = 0; i < adjacent.length; i++) {
            if (!possibleDirections[i + 1]) continue;

            AbilityTreeLocation adjacentLocation = adjacent[i];
            if (nodeMap.containsKey(adjacentLocation)) {
                locations.add(adjacentLocation);
            }
        }

        return locations;
    }

    public void saveToDisk(File saveFolder) {
        // Save the dump to a file
        JsonElement element = Managers.Json.GSON.toJsonTree(this);

        String fileName = Models.Character.getClassType().getName().toLowerCase(Locale.ROOT) + "_ablities.json";
        File jsonFile = new File(saveFolder, fileName);
        Managers.Json.savePreciousJson(jsonFile, element.getAsJsonObject());

        McUtils.sendMessageToClient(Component.literal("Saved ability tree dump to " + jsonFile.getAbsolutePath()));
    }

    // endregion

    public List<AbilityTreeSkillNode> getNodes() {
        return nodes;
    }
}
