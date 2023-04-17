/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
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
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");

    private final List<AbilityTreeSkillNode> nodes = new ArrayList<>();

    // Do not serialize this field
    private final transient List<AbilityTreeLocation> unprocessedConnections = new ArrayList<>();

    public void addNodeFromItem(ItemStack itemStack, int page, int slot) {
        nodes.add(Models.AbilityTree.ABILITY_TREE_PARSER
                .parseNodeFromItem(itemStack, page, slot, nodes.size() + 1)
                .key());
    }

    public void addConnectionFromItem(int page, int slot) {
        unprocessedConnections.add(AbilityTreeLocation.fromSlot(slot, page));
    }

    public void processItem(ItemStack itemStack, int page, int slot, boolean processConnections) {
        if (Models.AbilityTree.ABILITY_TREE_PARSER.isNodeItem(itemStack, slot)) {
            addNodeFromItem(itemStack, page, slot);
            return;
        }

        if (processConnections && Models.AbilityTree.ABILITY_TREE_PARSER.isConnectionItem(itemStack)) {
            addConnectionFromItem(page, slot);
        }
    }

    public void processConnections(int currentPage, boolean lastPage) {
        List<AbilityTreeLocation> processedLocation = new ArrayList<>();

        // We must traverse the connections in a specific order, so we sort them
        List<AbilityTreeLocation> sortedConnections =
                unprocessedConnections.stream().sorted().toList();

        for (AbilityTreeLocation location : sortedConnections) {
            if (processedLocation.contains(location)) continue;

            List<AbilityTreeLocation> connectedLocations = new ArrayList<>();
            connectedLocations.add(location);

            for (AbilityTreeLocation other : sortedConnections) {
                if (other.equals(location)) {
                    continue;
                }

                if (connectedLocations.stream().anyMatch(connected -> connected.isNeighbor(other))) {
                    connectedLocations.add(other);
                }
            }

            Set<AbilityTreeSkillNode> connectedNodes = new HashSet<>();
            for (AbilityTreeLocation connection : connectedLocations) {
                for (AbilityTreeSkillNode node : nodes) {
                    if (node.location().isNeighbor(connection)) {
                        connectedNodes.add(node);
                    }
                }
            }

            // If the connection is not valid, or continues to the next page, we keep it for next page (if we are not on
            // the last page)
            if (connectedNodes.size() <= 1
                    || (!lastPage
                            && connectedLocations.stream()
                                    .anyMatch(loc -> loc.page() == currentPage
                                            && loc.row() + 1 == AbilityTreeLocation.MAX_ROWS))) {
                continue;
            }

            for (AbilityTreeSkillNode current : connectedNodes) {
                current.connections()
                        .addAll(connectedNodes.stream()
                                .filter(node -> !node.equals(current))
                                .map(AbilityTreeSkillNode::id)
                                .toList());
            }

            processedLocation.addAll(connectedLocations);
        }

        // Remove the processed connections
        unprocessedConnections.removeAll(processedLocation);
    }

    public List<AbilityTreeSkillNode> getNodes() {
        return nodes;
    }

    public void saveToDisk() {
        if (!unprocessedConnections.isEmpty()) {
            McUtils.sendMessageToClient(Component.literal(
                    "WARN: There are unprocessed connections left in the dump! Check processing algorithm!"));
        }

        // Save the dump to a file
        JsonElement element = Managers.Json.GSON.toJsonTree(this);

        String fileName = Models.Character.getClassType().getName().toLowerCase(Locale.ROOT) + "_ablities.json";
        File jsonFile = new File(SAVE_FOLDER, fileName);
        Managers.Json.savePreciousJson(jsonFile, element.getAsJsonObject());

        McUtils.sendMessageToClient(Component.literal("Saved ability tree dump to " + jsonFile.getAbsolutePath()));
    }
}
