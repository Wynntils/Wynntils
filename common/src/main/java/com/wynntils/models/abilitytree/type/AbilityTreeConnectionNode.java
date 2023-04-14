/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Models;
import net.minecraft.world.item.ItemStack;

public class AbilityTreeConnectionNode {
    private AbilityTreeConnectionType connectionType;
    private AbilityTreeSkillNode[] nodes; // {up, down, left, right}
    private boolean[] connections = new boolean[4];

    public AbilityTreeConnectionNode(AbilityTreeConnectionType connectionType, AbilityTreeSkillNode[] nodes) {
        this.connectionType = connectionType;
        this.nodes = nodes;

        for (int i = 0; i < nodes.length; i++) {
            if (Models.AbilityTree.getNodeState(nodes[i]) == AbilityTreeNodeState.UNLOCKED) {
                connections[i] = true;
            }
        }
    }

    public ItemStack getItemStack() {
        return connectionType.getItemStack(connections);
    }
}
