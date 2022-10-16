/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class IngredientItemStack extends WynnItemStack {
    private final boolean isGuideStack;

    private final List<Component> guideTooltip;

    public IngredientItemStack(ItemStack stack) {
        super(stack);

        isGuideStack = false;
        guideTooltip = List.of();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        return isGuideStack ? guideTooltip : super.getTooltipLines(player, isAdvanced);
    }
}
