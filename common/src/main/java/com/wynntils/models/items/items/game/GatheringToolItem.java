/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.models.items.properties.ProfessionItemProperty;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.profession.type.ToolProfile;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class GatheringToolItem extends GameItem
        implements NumberedTierItemProperty, DurableItemProperty, LeveledItemProperty, ProfessionItemProperty {
    private final ToolProfile toolProfile;
    private final CappedValue durability;

    public GatheringToolItem(int emeraldPrice, ToolProfile toolProfile, CappedValue durability) {
        super(emeraldPrice);
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
    public List<ProfessionType> getProfessionTypes() {
        return List.of(toolProfile.toolType().getProfessionType());
    }

    @Override
    public String toString() {
        return "GatheringToolItem{" + "toolProfile="
                + toolProfile + ", durability="
                + durability + ", emeraldPrice="
                + emeraldPrice + '}';
    }
}
