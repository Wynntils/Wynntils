/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;

public class AbilityTreeNodeItem extends GuiItem {
    private final StyledText name;
    private final AbilityTreeNodeType abilityTreeNodeType;

    public AbilityTreeNodeItem(StyledText name, AbilityTreeNodeType abilityTreeNodeType) {
        this.name = name;
        this.abilityTreeNodeType = abilityTreeNodeType;
    }

    public StyledText getName() {
        return name;
    }

    public AbilityTreeNodeType getAbilityTreeNodeType() {
        return abilityTreeNodeType;
    }

    @Override
    public String toString() {
        return "AbilityTreeNodeItem{" + "abilityTreeNodeType=" + abilityTreeNodeType + '}';
    }
}
