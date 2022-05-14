/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class WynnItemStack extends ItemStack {

    protected String itemName;

    protected WynnItemStack(ItemStack stack) {
        super(stack.getItem(), stack.getCount());
        if (stack.getTag() != null) setTag(stack.getTag());

        itemName = WynnUtils.normalizeBadString(
                ChatFormatting.stripFormatting(super.getHoverName().getString()));
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        return super.getTooltipLines(player, flag);
    }
}
