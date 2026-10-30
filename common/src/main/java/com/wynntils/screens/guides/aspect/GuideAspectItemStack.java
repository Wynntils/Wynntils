/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.AspectTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class GuideAspectItemStack extends GuideItemStack {
    private final AspectInfo aspectInfo;
    private final int tier;
    private List<Component> generatedTooltip;

    public GuideAspectItemStack(AspectInfo aspectInfo, int tier) {
        super(aspectInfo.itemMaterial().itemStack(), new AspectItem(aspectInfo, tier), aspectInfo.name());

        this.aspectInfo = aspectInfo;
        this.tier = tier;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            generatedTooltip = AspectTooltipBuilder.buildNewItem(aspectInfo, tier, ItemSourceType.GUIDE, "")
                    .getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    public AspectInfo getAspectInfo() {
        return aspectInfo;
    }

    public int getTier() {
        return tier;
    }
}
