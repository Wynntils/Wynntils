/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;

public abstract class WynnItemStack extends ItemStack {

    protected String itemName;

    protected WynnItemStack(ItemStack stack) {
        super(stack.getItem(), stack.getCount());
        if (stack.getTag() != null) setTag(stack.getTag());

        itemName = WynnUtils.normalizeBadString(
                ChatFormatting.stripFormatting(super.getHoverName().getString()));
    }

    public String getSimpleName() {
        return itemName;
    }

    public List<Component> getOriginalTooltip() {
        return super.getTooltipLines(null, Default.NORMAL);
    }
}
