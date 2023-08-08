/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.models.profession.type.ToolProfile;
import com.wynntils.utils.type.CappedValue;

public class GatheringToolItem extends GameItem
        implements NumberedTierItemProperty, DurableItemProperty, LeveledItemProperty {
    private final ToolProfile toolProfile;
    private final CappedValue durability;

    public GatheringToolItem(ToolProfile toolProfile, CappedValue durability) {
        this.toolProfile = toolProfile;
        this.durability = durability;
    }

    public ToolProfile getToolProfile() {
        return toolProfile;
    }

    @Override
    public CappedValue getDurability() {
        return durability;
    }

    @Override
    public int getTier() {
        return toolProfile.tier();
    }

    @Override
    public int getLevel() {
        return toolProfile.getLevel();
    }

    @Override
    public String toString() {
        return "GatheringToolItem{" + "toolProfile=" + toolProfile + ", durability=" + durability + '}';
    }
}
