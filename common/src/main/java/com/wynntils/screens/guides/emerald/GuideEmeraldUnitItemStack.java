/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.EmeraldUnitTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.game.EmeraldItem;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GuideEmeraldUnitItemStack extends GuideEmeraldItemStack {
    private final EmeraldUnits unit;

    public GuideEmeraldUnitItemStack(EmeraldUnits unit) {
        super(unit.getItemStack(), new EmeraldItem(() -> 1, unit), unit.name());
        this.unit = unit;
    }

    public EmeraldUnits getEmeraldUnit() {
        return unit;
    }

    @Override
    public List<Component> buildTooltip() {
        return EmeraldUnitTooltipBuilder.buildNewItem(unit, ItemSourceType.GUIDE, "")
                .getTooltipLines(Models.Character.getClassType());
    }

    @Override
    public int getTier() {
        return -1;
    }
}
