/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Models;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public class AbilityTreeConnectionNode {
    private AbilityTreeConnectionType connectionType;

    // {up, down, left, right}
    // There can be multiple suppliers from a side
    private Set<AbilityTreeSkillNode>[] nodes;
    private boolean[] connections = new boolean[4];

    public AbilityTreeConnectionNode(AbilityTreeConnectionType connectionType, AbilityTreeSkillNode[] nodes) {
        this.connectionType = connectionType;
        this.nodes = Arrays.stream(nodes).map(Collections::singleton).toArray(Set[]::new);

        updateConnectedStates();
    }

    private AbilityTreeConnectionNode(AbilityTreeConnectionType connectionType, Set<AbilityTreeSkillNode>[] nodes) {
        this.connectionType = connectionType;
        this.nodes = nodes;

        updateConnectedStates();
    }

    private void updateConnectedStates() {
        for (int i = 0; i < nodes.length; i++) {
            connections[i] = false;

            for (AbilityTreeSkillNode nodeInDirection : nodes[i]) {
                if (Models.AbilityTree.getNodeState(nodeInDirection) == AbilityTreeNodeState.UNLOCKED) {
                    connections[i] = true;
                    break;
                }
            }
        }
    }

    public ItemStack getItemStack() {
        // FIXME: Try to do this less times
        updateConnectedStates();
        return connectionType.getItemStack(connections);
    }

    public AbilityTreeConnectionType getConnectionType() {
        return connectionType;
    }

    public AbilityTreeConnectionNode merge(AbilityTreeConnectionNode other) {
        if (other == null) {
            return this;
        }

        AbilityTreeConnectionType newConnectionType;

        if (this.getConnectionType() == other.getConnectionType()) {
            // Connection type is the same, we only need to merge the nodes
            newConnectionType = connectionType;
        } else {
            // Connection type is different, we need to merge the nodes and the connection type
            newConnectionType = AbilityTreeConnectionType.merge(connectionType, other.getConnectionType());
        }

        Set<AbilityTreeSkillNode>[] newNodes = new HashSet[4];

        for (int i = 0; i < nodes.length; i++) {
            Set<AbilityTreeSkillNode> nodes = this.nodes[i];
            newNodes[i] = new HashSet<>(nodes);
        }

        Set<AbilityTreeSkillNode>[] sets = other.nodes;
        for (int i = 0; i < sets.length; i++) {
            Set<AbilityTreeSkillNode> nodes = sets[i];
            newNodes[i].addAll(nodes);
        }

        // Return a merged instance
        return new AbilityTreeConnectionNode(newConnectionType, newNodes);
    }
}
