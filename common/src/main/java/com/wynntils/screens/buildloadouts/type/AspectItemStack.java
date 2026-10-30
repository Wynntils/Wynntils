/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.type;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.AspectTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.aspects.type.AspectInfo;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AspectItemStack extends ItemStack {
    private final AspectTooltipBuilder tooltipBuilder;

    public AspectItemStack(AspectInfo aspectInfo, int tier) {
        super(aspectInfo.itemMaterial().itemStack().getItem(), 1);

        applyComponents(aspectInfo.itemMaterial().itemStack().getComponentsPatch());
        tooltipBuilder = AspectTooltipBuilder.buildNewItem(aspectInfo, tier, ItemSourceType.ITEM, "");
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        return tooltipBuilder.getTooltipLines(Models.Character.getClassType());
    }
}
