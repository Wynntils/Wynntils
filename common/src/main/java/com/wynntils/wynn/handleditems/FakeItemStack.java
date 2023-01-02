/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems;

import com.wynntils.handlers.item.AnnotatedItemStack;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.utils.GearTooltipBuilder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FakeItemStack extends ItemStack {
    private final GearItem gearItem;
    private final String source;

    public FakeItemStack(GearItem gearItem, String source) {
        super(gearItem.getItemProfile().getItemInfo().asItemStack().getItem(), 1);
        this.source = source;
        ((AnnotatedItemStack) this).setAnnotation(gearItem);
        this.gearItem = gearItem;
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        GearTooltipBuilder tooltipBuilder = new GearTooltipBuilder(gearItem);
        List<Component> tooltip = tooltipBuilder.getTooltipLines();
        tooltip.add(
                1, Component.literal(source).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        return tooltip;
    }
}
