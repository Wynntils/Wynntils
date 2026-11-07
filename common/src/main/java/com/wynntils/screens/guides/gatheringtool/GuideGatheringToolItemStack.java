/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gatheringtool;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.GatheringToolTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.profession.type.GatheringToolInfo;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideGatheringToolItemStack extends GuideItemStack {
    private List<Component> generatedTooltip;
    private final GatheringToolInfo toolInfo;

    public GuideGatheringToolItemStack(GatheringToolInfo toolInfo) {
        super(toolInfo.material().itemStack(), new GatheringToolItem(toolInfo, CappedValue.EMPTY), toolInfo.name());

        this.toolInfo = toolInfo;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            generatedTooltip = GatheringToolTooltipBuilder.buildNewItem(toolInfo, ItemSourceType.GUIDE, "")
                    .getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    public GatheringToolInfo getGatheringToolInfo() {
        return toolInfo;
    }
}
