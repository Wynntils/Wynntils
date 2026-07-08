/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.models.items.properties.ProfessionItemProperty;
import com.wynntils.models.profession.type.GatheringToolInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class GatheringToolItem extends GameItem
        implements NumberedTierItemProperty,
                DurableItemProperty,
                GearTierItemProperty,
                LeveledItemProperty,
                ProfessionItemProperty {
    private final GatheringToolInfo toolInfo;
    private final CappedValue durability;

    public GatheringToolItem(GatheringToolInfo toolInfo, CappedValue durability) {
        this.toolInfo = toolInfo;
        this.durability = durability;
    }

    public GatheringToolInfo getToolInfo() {
        return toolInfo;
    }

    @Override
    public CappedValue getDurability() {
        return durability;
    }

    @Override
    public int getTier() {
        return toolInfo.tier();
    }

    @Override
    public int getLevel() {
        return toolInfo.level();
    }

    @Override
    public String toString() {
        return "GatheringToolItem{" + "toolinfo=" + toolInfo + ", durability=" + durability + '}';
    }

    @Override
    public List<ProfessionType> getProfessionTypes() {
        return List.of(toolInfo.professionType());
    }

    @Override
    public GearTier getGearTier() {
        return toolInfo.gearTier();
    }
}
