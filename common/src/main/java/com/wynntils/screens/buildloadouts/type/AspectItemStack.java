/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.type;

import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.screens.guides.aspect.GuideAspectItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class AspectItemStack extends GuideAspectItemStack {
    public AspectItemStack(AspectInfo aspectInfo, int tier) {
        super(aspectInfo, tier);
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        return new ArrayList<>(generatedTooltip);
    }
}
