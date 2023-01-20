/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gathering.ToolProfile;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.utils.CappedValue;

public class GatheringToolItem extends GameItem implements NumberedTierItemProperty, DurableItemProperty {
    private final ToolProfile toolProfile;
    private final CappedValue durability;

    public GatheringToolItem(ToolProfile toolProfile, CappedValue durability) {
        this.toolProfile = toolProfile;
        this.durability = durability;
    }

    public ToolProfile getToolProfile() {
        return toolProfile;
    }

    public CappedValue getDurability() {
        return durability;
    }

    public int getTier() {
        return toolProfile.tier();
    }

    @Override
    public String toString() {
        return "GatheringToolItem{" + "toolProfile=" + toolProfile + ", durability=" + durability + '}';
    }
}
