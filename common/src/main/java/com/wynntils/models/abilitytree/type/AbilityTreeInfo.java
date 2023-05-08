/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.google.common.collect.ComparisonChain;
import com.google.gson.JsonElement;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * This class contains all relevant info to a specific class' ability tree.
 */
public class AbilityTreeInfo {
    private final List<AbilityTreeSkillNode> nodes = new ArrayList<>();

    // Do not serialize this field
    private final transient List<AbilityTreeConnectionHolder> unprocessedConnections = new ArrayList<>();

    public void addNodeFromItem(ItemStack itemStack, int page, int slot) {
        nodes.add(Models.AbilityTree.ABILITY_TREE_PARSER
                .parseNodeFromItem(itemStack, page, slot, nodes.size() + 1)
                .key());
    }

    public void addConnectionFromItem(ItemStack itemStack, int page, int slot) {
        unprocessedConnections.add(new AbilityTreeConnectionHolder(
                AbilityTreeConnectionType.fromDamage(itemStack.getDamageValue()),
                AbilityTreeLocation.fromSlot(slot, page)));
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

    public void processConnections(int currentPage, boolean lastPage) {
        List<AbilityTreeConnectionHolder> processedConnections = new ArrayList<>();

        // We must traverse the connections in a specific order, so we sort them
        List<AbilityTreeConnectionHolder> sortedConnections =
                unprocessedConnections.stream().sorted().toList();

        for (AbilityTreeConnectionHolder holder : sortedConnections) {
            if (processedConnections.contains(holder)) continue;

            List<AbilityTreeConnectionHolder> connectedLocations = new ArrayList<>();
            connectedLocations.add(holder);

            for (AbilityTreeConnectionHolder other : sortedConnections) {
                if (other.equals(holder)) {
                    continue;
                }

                if (connectedLocations.stream().anyMatch(connected -> connected.isNeighbor(other.location()))) {
                    connectedLocations.add(other);
                }
            }

            Set<AbilityTreeSkillNode> connectedNodes = new HashSet<>();
            for (AbilityTreeConnectionHolder connection : connectedLocations) {
                for (AbilityTreeSkillNode node : nodes) {
                    if (connection.isNeighbor(node.location())) {
                        connectedNodes.add(node);
                    }
                }
            }

            if (connectedNodes.size() <= 1) continue;

            // If the connection continues to the next page,
            // we process it in the next page (if we are not on the last page)
            boolean connectionContinuesInNextPage = connectedLocations.stream()
                    .map(AbilityTreeConnectionHolder::location)
                    .anyMatch(loc -> loc.page() == currentPage && loc.row() + 1 == AbilityTreeLocation.MAX_ROWS);

            if (!lastPage && connectionContinuesInNextPage) continue;

            for (AbilityTreeSkillNode current : connectedNodes) {
                current.connections()
                        .addAll(connectedNodes.stream()
                                .filter(node -> !node.equals(current))
                                .map(AbilityTreeSkillNode::id)
                                .toList());
            }

            processedConnections.addAll(connectedLocations);
        }

        // Remove the processed connections
        unprocessedConnections.removeAll(processedConnections);
    }

    public List<AbilityTreeSkillNode> getNodes() {
        return nodes;
    }

    public void saveToDisk(File saveFolder) {
        if (!unprocessedConnections.isEmpty()) {
            McUtils.sendMessageToClient(Component.literal(
                    "WARN: There are unprocessed connections left in the dump! Check processing algorithm!"));
        }

        // Save the dump to a file
        JsonElement element = Managers.Json.GSON.toJsonTree(this);

        String fileName = Models.Character.getClassType().getName().toLowerCase(Locale.ROOT) + "_ablities.json";
        File jsonFile = new File(saveFolder, fileName);
        Managers.Json.savePreciousJson(jsonFile, element.getAsJsonObject());

        McUtils.sendMessageToClient(Component.literal("Saved ability tree dump to " + jsonFile.getAbsolutePath()));
    }

    private record AbilityTreeConnectionHolder(AbilityTreeConnectionType connectionType, AbilityTreeLocation location)
            implements Comparable<AbilityTreeConnectionHolder> {
        public boolean isNeighbor(AbilityTreeLocation other) {
            boolean[] possibleDirections = this.connectionType.getPossibleDirections();

            if (possibleDirections[0] && this.location.getAbsoluteRow() - 1 == other.getAbsoluteRow()) {
                return this.location.col() == other.col();
            }
            if (possibleDirections[1] && this.location.col() + 1 == other.col()) {
                return this.location.getAbsoluteRow() == other.getAbsoluteRow();
            }
            if (possibleDirections[2] && this.location.getAbsoluteRow() + 1 == other.getAbsoluteRow()) {
                return this.location.col() == other.col();
            }
            if (possibleDirections[3] && this.location.col() - 1 == other.col()) {
                return this.location.getAbsoluteRow() == other.getAbsoluteRow();
            }

            return false;
        }

        @Override
        public int compareTo(AbilityTreeInfo.AbilityTreeConnectionHolder other) {
            return ComparisonChain.start()
                    .compare(this.location(), other.location())
                    .result();
        }
    }
}
