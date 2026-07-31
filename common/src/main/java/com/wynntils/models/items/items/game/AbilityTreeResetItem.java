package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;

public class AbilityTreeResetItem extends GameItem {
    private final boolean canReset;

    public AbilityTreeResetItem(boolean canReset) {
        this.canReset = canReset;
    }

    public boolean getCanReset() {
        return canReset;
    }

    @Override
    public String toString() {
        return "AbilityTreeResetItem{" + "canReset=" + canReset + '}';
    }

}
