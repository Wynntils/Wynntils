/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.EmeraldPouchTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GuideEmeraldPouchItemStack extends GuideEmeraldItemStack {
    private final int tier;

    public GuideEmeraldPouchItemStack(int tier) {
        super(new ItemStack(Items.DIAMOND_AXE), new EmeraldPouchItem(tier, 0), "Emerald Pouch");
        this.setDamageValue(97);

        this.tier = tier;

        this.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
    }

    @Override
    public List<Component> buildTooltip() {
        return EmeraldPouchTooltipBuilder.buildNewItem(tier, ItemSourceType.GUIDE, "")
                .getTooltipLines(Models.Character.getClassType());
    }

    @Override
    public int getTier() {
        return tier;
    }
}
