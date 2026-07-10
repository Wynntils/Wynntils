/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText;

public class AbilityShardItem extends GameItem {
    private final StyledText name;
    private final boolean questRequirement;

    public AbilityShardItem(StyledText name, boolean questRequirement) {
        this.name = name;
        this.questRequirement = questRequirement;
    }

    public StyledText getName() {
        return name;
    }

    public boolean getQuestRequirement() {
        return questRequirement;
    }

    @Override
    public String toString() {
        return "AbilityShardItem{" + "questRequirement=" + questRequirement + '}';
    }
}
